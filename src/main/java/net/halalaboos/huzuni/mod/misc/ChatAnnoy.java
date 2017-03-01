package net.halalaboos.huzuni.mod.misc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.event.UpdateEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.settings.Toggleable;
import net.halalaboos.huzuni.api.util.Timer;
import net.halalaboos.mcwrapper.api.block.Block;
import net.halalaboos.mcwrapper.api.block.BlockTypes;
import net.halalaboos.mcwrapper.api.entity.Entity;
import net.halalaboos.mcwrapper.api.entity.ExperienceOrb;
import net.halalaboos.mcwrapper.api.entity.ItemPickup;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.network.NetworkHandler;
import net.halalaboos.mcwrapper.api.network.PlayerInfo;
import net.halalaboos.mcwrapper.api.network.packet.server.ItemPickupPacket;
import net.halalaboos.mcwrapper.api.util.AssetLocation;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.halalaboos.mcwrapper.api.MCWrapper.*;

/**
 * Randomly sends messages in chat under certain circumstances to annoy other players.
 *
 * TODO: Remove minecraft code
 */
public class ChatAnnoy extends BasicMod {

	//Toggles
	private final Toggleable pickup = new Toggleable("Pickups", "Randomly post in chat when you pick up an item.");
	private final Toggleable dig = new Toggleable("Mining", "Randomly post in chat when you break a block.");
	private final Toggleable time = new Toggleable("Time", "Randomly post the time in chat.");

	//Timer used to delay the sent messages
	private final net.halalaboos.huzuni.api.util.Timer timer = new Timer();

	//Random to also delay the messages and make them not send every time something happens
	private final Random random = new Random();

	//The messages to send - Category (e.g. dig, time, pickup), Messages
	private final Map<String, List<String>> messageMap = new HashMap<>();

	//The last sent messages - Category, Message
	private final Map<String, String> lastSentMessages = new HashMap<>();

	public ChatAnnoy() {
		super("Chat annoy", "Annoy others in chat!");
		setAuthor("brudin");
		setCategory(Category.MISC);
		addChildren(pickup, dig, time);
		dig.setEnabled(true);
		pickup.setEnabled(true);
		lastSentMessages.put("pickup", "");
		lastSentMessages.put("dig", "");
	}

	@Override
	protected void onEnable() {
		lastSentMessages.put("time", getCurrentTime());
		huzuni.eventManager.addListener(this);
		if (messageMap.isEmpty()) {
			try {
				loadMessages(getMinecraft().getInputStream(new AssetLocation("huzuni/chatannoy.json")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDisable() {
		huzuni.eventManager.removeListener(this);
	}

	/**
	 * Performed when packets are read/sent, used to call the actual annoy modes.
	 */
	@EventMethod
	public void onPacket(PacketEvent event) {
		if (event.type == PacketEvent.Type.SENT) {
			if (timer.hasReach(1500) && random.nextBoolean()) {
				if (dig.isEnabled() && event.getPacket() instanceof CPacketPlayerDigging) {
					//todo dig wrapper
					CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();
					alertDigging(packet);
				}
			}
		} else {
			if (pickup.isEnabled() && timer.hasReach(250) && random.nextBoolean()) {
				if (event.getPacket() instanceof ItemPickupPacket) {
					ItemPickupPacket packet = (ItemPickupPacket) event.getPacket();
					alertPickup(packet);
				}
			}
		}
	}

	@EventMethod
	public void onTick(UpdateEvent event) {
		if (event.type == UpdateEvent.Type.PRE) {
			if (timer.hasReach(1500) && time.isEnabled()) {
				alertTime();
			}
		}
	}

	/**
	 * Randomly sends a chat message with the time.
	 *
	 * <p>This will only send the message if there is at least a one minute difference between the last sent
	 * time.</p>
	 */
	private void alertTime() {
		if (!getCurrentTime().equals(lastSentMessages.get("time"))) {
			getPlayer().sendMessage(getMessage("time", null, null));
			lastSentMessages.put("time", getCurrentTime());
			timer.reset();
		}
	}

	/**
	 * Randomly sends a chat message when the Player mines a Block.
	 * @param digging The digging packet
	 */
	private void alertDigging(CPacketPlayerDigging digging) {
		//Check if the player has (pretty much) broken a block and also an additional random check
		if (getController().getBlockDamage() >= 0.9F && random.nextBoolean()) {
			BlockPos pos = digging.getPosition();
			//The block the Player broke
			Block block = getWorld().getBlock(new Vector3i(pos.getX(), pos.getY(), pos.getZ()));
			//The name of the Block
			String name = block.name();
			//Check if the Block isn't air and the last block we sent to chat isn't the same as this one
			if (block != BlockTypes.AIR && !name.equals(lastSentMessages.get("block"))) {
				//Sends the message
				getPlayer().sendMessage(getMessage("dig", block, null));
				//Set the last block name to the current block name
				lastSentMessages.put("dig", block.name());
			}
			//Reset the timer.
			timer.reset();
		}
	}

	/**
	 * Randomly sends a chat message when the Player picks up an Item.
	 * @param collectItem The pickup packet
	 */
	private void alertPickup(ItemPickupPacket collectItem) {
		int collector = collectItem.getCollector();
		int collected = collectItem.getCollected();

		//Check if it was you who picked up the item
		if (getPlayer().getId() == collector) {
			//The picked up object
			Entity entity = getWorld().getEntity(collected);
			//Check if the item is an item and not an experience orb or something
			if (entity instanceof ItemPickup && !(entity instanceof ExperienceOrb)) {
				//The item entity
				ItemPickup pickup = ((ItemPickup) entity);
				//The actual item stack that will be obtained
				ItemStack itemStack = pickup.getItem();

				//Check if the name isn't the last sent item name
				if (!itemStack.name().equals(lastSentMessages.get("pickup"))) {
					//Send the alert in chat
					getPlayer().sendMessage(getMessage("pickup", null, itemStack));
					//Set the last item to this one
					lastSentMessages.put("pickup", itemStack.name());
				}
			}
		}
		//Reset the timer
		timer.reset();
	}

	/**
	 * Returns the current time, used in the {@link #time} mode.
	 * @return Time in HH:MM AM/PM format
	 */
	private String getCurrentTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
	}

	/**
	 * Picks a random chat message from the {@link #messageMap} based on the specified category.
	 *
	 * @param category The category of the message, such as dig/pickup/time.
	 * @param block If the category is dig, then this would represent the mined block.
	 * @param itemStack If the category is pickup, then this would represent the picked up item.
	 * @return The message to send
	 */
	private String getMessage(String category, @Nullable Block block, @Nullable ItemStack itemStack) {
		int size = messageMap.get(category).size();
		String message = messageMap.get(category).get(random.nextInt(size));
		message = message.replace("$PLAYER", getRandomPlayer()).replace("$TIME", getCurrentTime());
		if (block != null) {
			message = message.replace("$BLOCK", block.name());
		}
		if (itemStack != null) {
			message = message.replace("$ITEM", itemStack.name());
		}
		return message;
	}

	/**
	 * Returns a random player's name.  Used for the $PLAYER string(s) in the various messages.
	 */
	private String getRandomPlayer() {
		Optional<NetworkHandler> networkHandler = getMinecraft().getNetworkHandler();

		//Checks if you're in singleplayer, then will just return "you"
		if (!networkHandler.isPresent() || !getMinecraft().isRemote())
			return "you";

		//The connected players
		List<PlayerInfo> list = new ArrayList<>(networkHandler.get().getPlayers());

		//A random player from the list
		PlayerInfo randomPlayer = list.get(random.nextInt(list.size()));

		//The current output, which is an unformatted version of the player's name
		String out = randomPlayer.getName(false);

		//Check if the output is the your name
		if (out.equals(getPlayer().name()))
			return getRandomPlayer(); //If so, try again!

		//Otherwise, return the output
		return out;
	}

	/**
	 * Used for parsing the JSON file, probably not necessary but keeps things tidy.
	 */
	private class ChatMessage {
		//The category of the message, e.g. dig/pickup/time
		String category;

		//The messages from this category
		List<String> messages;
	}

	/**
	 * Used to load the messages from the chatannoy.json file.  Probably will make an actual JSON helper class to make
	 * this not necessary.
	 */
	private void loadMessages(InputStream inputStream) {
		try {
			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			Type type = new TypeToken<List<ChatMessage>>(){}.getType();
			List<ChatMessage> chatMessage = gson.fromJson(reader, type);
			for (ChatMessage msg : chatMessage) {
				messageMap.put(msg.category, msg.messages);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}