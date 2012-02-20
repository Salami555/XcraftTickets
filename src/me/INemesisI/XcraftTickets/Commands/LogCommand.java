package me.INemesisI.XcraftTickets.Commands;

import java.util.List;

import me.INemesisI.XcraftTickets.Ticket;
import me.INemesisI.XcraftTickets.XcraftTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LogCommand extends CommandHelper {

	protected LogCommand(XcraftTickets instance) {
		super(instance);
	}

	@Override
	protected void execute(CommandSender sender, String Command, List<String> list) {
		this.init(sender);

		if (list.size() < 1 || !list.get(0).matches("\\d*")) {
			error("Du hast keine Ticketnummer angegeben" + "\n" + ChatColor.GRAY + "(/ticket comment <#> <Nachricht>)");
			return;
		}
		if (list.size() < 2) {
			error("Du hast keine Nachricht eingeben!" + "\n" + ChatColor.GRAY + "(/ticket comment <#> <Nachricht>)");
			return;
		}
		Ticket ticket = th.getTicket(Integer.parseInt(list.get(0)));
		if (ticket == null) {
			error("Ein Ticket mit dieser Nummer existier nicht!");
			return;
		}
		boolean isMod;
		isMod = senderHasPermission("XcraftTickets.List.All");
		if (!ticket.getOwner().equals(getName()) && !isMod) {
			error("Du hast keine Rechte dieses Ticket kommentieren!" + ChatColor.GRAY + " \nEs ist nicht dein Ticket...");
			return;
		}
		String message = list.subList(1, list.size()).toString().replace(",", "").replace("[", "").replace("]", "");
		th.LogTicket(ticket, getName(), "Kommentiert", message);

		ticket.getWatched().clear();
		ticket.getWatched().add(getName());

		sendToMods(ChatColor.GRAY + "Ticket " + ChatColor.GOLD + "#" + ticket.getId() + ChatColor.GRAY + " wurde von " + ChatColor.YELLOW + getName() + ChatColor.GRAY + " kommentiert: " + ChatColor.AQUA + message);
		sendToPlayer(ticket.getOwner(), ChatColor.GRAY + "Dein Ticket " + ChatColor.GOLD + "#" + ticket.getId() + ChatColor.GRAY + " wurde von " + ChatColor.YELLOW + getName() + ChatColor.GRAY + " kommentiert: " + ChatColor.AQUA + message);
	}
}