package net.halalaboos.huzuni.api.mod.command.impl;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.huzuni.api.mod.command.Command;
import net.halalaboos.huzuni.api.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic implementation of the command interface. Allows for sub commands.
 * */
public abstract class BasicCommand implements Command {
	
	protected static final Huzuni huzuni = Huzuni.INSTANCE;
	
	private final List<Command> commands = new ArrayList<>();
	
	private final String[] aliases;
	
	private final String description;
	
	public BasicCommand(String alias, String description) {
		this.aliases = new String[] { alias };
		this.description = description;
	}
	
	public BasicCommand(String[] aliases, String description) {
		this.aliases = aliases;
		this.description = description;
	}
	
	@Override
	public String[] getAliases() {
		return aliases;
	}

	@Override
	public void giveHelp() {
		huzuni.addChatMessage("No help is provided!");
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void run(String input, String[] args) throws Exception {
		if (args != null && args.length >= 1 && !commands.isEmpty()) {
			for (Command command : commands) {
				for (String alias : command.getAliases()) {
					if (alias.toLowerCase().equals(args[0].toLowerCase())) {
						command.run(StringUtils.getAfter(input, 1), args.length - 1 > 0 ? Arrays.copyOfRange(args, 1, args.length) : null);
						return;
					}
				}	
			}
			runCommand(input, args);
		} else
			runCommand(input, args);
	}
	
	/**
	 * 
	 * */
	protected abstract void runCommand(String input, String[] args) throws Exception;
	
	public BasicCommand addSubcommand(Command command) {
		this.commands.add(command);
		return this;
	}

}
