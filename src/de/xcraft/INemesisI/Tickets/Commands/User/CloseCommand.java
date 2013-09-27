package de.xcraft.INemesisI.Tickets.Commands.User;

import org.bukkit.command.CommandSender;

import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
import de.xcraft.INemesisI.Tickets.Log.EntryType;
import de.xcraft.INemesisI.Tickets.Msg;
import de.xcraft.INemesisI.Tickets.Msg.Replace;
import de.xcraft.INemesisI.Tickets.Ticket;
import de.xcraft.INemesisI.Tickets.Manager.TicketManager;

public class CloseCommand extends XcraftCommand {

	public CloseCommand() {
		super("ticket", "close", "c.*", "<ID> <MESSAGE> ...", Msg.COMMAND_CLOSE.toString(), "XcraftTickets.Close");
	}

	@Override
	public boolean execute(XcraftPluginManager pManager, CommandSender sender, String[] args) {
		TicketManager manager = (TicketManager) pManager;
		int id = Integer.parseInt(args[0]);
		Ticket ticket = manager.getTicket(id);
		if (ticket == null) {
			pManager.plugin.getMessenger().sendInfo(sender, Msg.ERR_TICKET_NOT_FOUND.toString(Replace.ID(id)), true);
			return true;
		}
		if (!ticket.getOwner().equals(sender.getName()) && !sender.hasPermission("XcraftTickets.Close.All")) {
			pManager.plugin.getMessenger().sendInfo(sender, Msg.ERR_TICKET_NO_PERMISSION.toString(), true);
			return true;
		}
		String message = manager.getMessage(sender, args);
		ticket.getLog().add(EntryType.CLOSE, sender.getName(), message);
		if (ticket.getId() == manager.getLastTicket(sender)) {
			manager.setLastTicket(sender, -1);
		}
		manager.setTicketArchived(ticket);
		Replace[] replace = { Replace.NAME(sender.getName()), Replace.ID(id), Replace.MESSAGE(message) };
		manager.inform(ticket, Msg.TICKET_BROADCAST_CLOSE.toString(replace), true);
		if (!sender.getName().equals(ticket.getOwner())) {
			manager.getPlugin().getConfigManager().addReminder(ticket.getOwner(), id);
		}
		return true;
	}
}