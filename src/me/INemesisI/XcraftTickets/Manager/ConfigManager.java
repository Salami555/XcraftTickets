package me.INemesisI.XcraftTickets.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import me.INemesisI.XcraftTickets.Log;
import me.INemesisI.XcraftTickets.Ticket;
import me.INemesisI.XcraftTickets.XcraftTickets;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	XcraftTickets plugin;
	File folder;
	File archive;
	FileConfiguration config;
	File remFile;
	FileConfiguration reminder;

	public ConfigManager(XcraftTickets instance) {
		plugin = instance;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		// initialization
		folder = plugin.getDataFolder();
		archive = new File(folder.getPath() + "/archive");
		if (!archive.exists()) {
			archive.mkdirs();
		}
		plugin.reloadConfig();
		config = plugin.getConfig();
		remFile = new File(folder, "reminder.yml");
		reminder = YamlConfiguration.loadConfiguration(remFile);
		// load tickets
		File[] files = folder.listFiles();
		Arrays.sort(files);
		List<Ticket> tickets = new ArrayList<Ticket>();
		for (File file : files) {
			Ticket ticket = this.loadTicket(file);
			if (ticket != null) {
				tickets.add(ticket);
			}
		}
		plugin.ticketManager.setTickets(tickets);
		plugin.ticketManager.setNextID(config.getInt("Next_Ticket_ID", 1));
		List<String> assignees = ((List<String>) config.getList("Assignee"));
		plugin.ticketManager.setAssignees(assignees);
		ConfigurationSection cs = config.getConfigurationSection("Phrases");
		Map<String, String> phrases = plugin.ticketManager.getPhrases();
		for (String value : cs.getKeys(false)) {
			phrases.put(value, cs.getString(value));
		}
	}
	public void save() {
		config.set("Next_Ticket_ID", plugin.ticketManager.getNextID());
		config.set("Assignee", plugin.ticketManager.getAssignees());
		config.set("Phrases", plugin.ticketManager.getPhrases());
		plugin.saveConfig();
		try {
			reminder.save(remFile);
		} catch (IOException e) {
		}
		for (Ticket ticket : plugin.ticketManager.getTickets()) {
			this.saveTicket(folder, ticket);
		}
	}

	@SuppressWarnings("unchecked")
	public Ticket loadTicket(File ticket) {
		String filename = ticket.getAbsoluteFile().getName();
		if (!filename.equals("config.yml") && !filename.equals("archive") && !filename.equals("reminder.yml")) {
			int id = Integer.parseInt(filename.replace(".yml", ""));
			FileConfiguration temp = YamlConfiguration.loadConfiguration(ticket);
			ConfigurationSection cs = temp.getConfigurationSection("Ticket");
			List<Log> log = new ArrayList<Log>();
			List<String> list = (List<String>) cs.getList("log");
			if ((list != null) && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					String split[] = list.get(i).split("; ");
					log.add(new Log(split[0], split[1], Log.Type.valueOf(split[2]), split[3]));
				}
			}
			List<String> watched = (ArrayList<String>) cs.getList("watched");
			if (watched == null) {
				watched = new ArrayList<String>();
			}
			String assignee = cs.getString("assignee");
			if ((assignee != null) && assignee.equals("none")) {
				assignee = null;
			}
			cs = temp.getConfigurationSection("Ticket.location");
			Location loc = null;
			String world = null;
			if (cs != null) {
				world = cs.getString("world");
				World w = plugin.getServer().getWorld(world);
				loc = new Location(w, cs.getLong("x"), cs.getLong("y"), cs.getLong("z"), cs.getLong("pitch"),
						cs.getLong("yaw"));
			}
			return new Ticket(id, assignee, loc, world, watched, log);
		}
		return null;
	}

	public void saveTicket(File folder, Ticket ticket) {
		File file = new File(folder, ticket.getId() + ".yml");
		FileConfiguration temp = YamlConfiguration.loadConfiguration(file);
		temp.set("Ticket.assignee", ticket.getAssignee());
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < ticket.getLog().size(); i++) {
			list.add(ticket.getLog().get(i).toString());
		}
		temp.set("Ticket.log", list);
		temp.set("Ticket.watched", ticket.getWatched());
		Location loc = ticket.getLoc();
		if (loc.getWorld() != null) {
			temp.set("Ticket.location.x", loc.getX());
			temp.set("Ticket.location.y", loc.getY());
			temp.set("Ticket.location.z", loc.getZ());
			temp.set("Ticket.location.pitch", loc.getPitch());
			temp.set("Ticket.location.yaw", loc.getYaw());
			temp.set("Ticket.location.world", ticket.getWorld());
		}
		try {
			temp.save(file);
		} catch (IOException e) {
		}
	}

	public void archiveTicket(Ticket ticket) {
		this.saveTicket(archive, ticket);
		this.deleteTicket(ticket);
	}

	public void deleteTicket(Ticket ticket) {
		File file = new File(folder, ticket.getId() + ".yml");
		file.delete();
	}

	@SuppressWarnings("unchecked")
	public void addReminder(String player, int id) {
		List<String> list = (List<String>) reminder.getList(player);
		if (list == null) {
			list = new ArrayList<String>();
		}
		list.add(String.valueOf(id));
		reminder.set(player, list);
	}

	@SuppressWarnings("unchecked")
	public boolean removeReminder(String player, int id) {
		String sid = String.valueOf(id);
		List<String> list = (List<String>) reminder.getList(player);
		if (list != null) {
			if ((list.remove(sid) == true)) {
				if (list.size() == 0) {
					reminder.set(player, null);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getReminder(String player) {
		return (List<String>) reminder.getList(player);
	}
}
