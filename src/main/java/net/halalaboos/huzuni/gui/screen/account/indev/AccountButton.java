package net.halalaboos.huzuni.gui.screen.account.indev;

import net.halalaboos.huzuni.api.account.Account;
import net.halalaboos.huzuni.indev.gui.components.Button;
import net.halalaboos.mcwrapper.api.util.enums.MouseButton;

import static net.halalaboos.mcwrapper.api.MCWrapper.getMinecraft;

/**
 * {@link net.halalaboos.huzuni.indev.gui.components.Button} used for displaying and logging into an {@link Account}.
 */
public class AccountButton extends Button {

	private final Account account;

	private final AccountScreen screen;

	AccountButton(Account account, AccountScreen screen) {
		super("account", account.isHidden() ? "(hidden)" : account.getUsername());
		this.account = account;
		this.screen = screen;

		onPressed((button, action) -> {
			MouseButton mouseButton = MouseButton.getMouseButton(action.buttonId);
			if (mouseButton == MouseButton.LEFT) {
				try {
					account.login();
					this.screen.status.setText("Logged in: " + getMinecraft().session().name());
				} catch (Exception e) {
					e.printStackTrace();
					this.screen.status.setText("Failed to login!");
				}
			} else if (mouseButton == MouseButton.RIGHT) {
				account.setHidden(!account.isHidden());
			}
			updateText();
			return true;
		});
	}

	private void updateText() {
		this.setText(!account.isHidden() ? account.getUsername() : "(hidden)");
	}
}