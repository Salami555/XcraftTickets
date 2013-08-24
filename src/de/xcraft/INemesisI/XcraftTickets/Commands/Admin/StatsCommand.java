package de.xcraft.INemesisI.XcraftTickets.Commands.Admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.command.CommandSender;

import de.xcraft.INemesisI.Utils.Command.XcraftCommand;
import de.xcraft.INemesisI.Utils.Manager.XcraftPluginManager;
import de.xcraft.INemesisI.XcraftTickets.Msg;
import de.xcraft.INemesisI.XcraftTickets.XcraftTickets;
import de.xcraft.INemesisI.XcraftTickets.Manager.TicketManager;

public class StatsCommand extends XcraftCommand {

	public StatsCommand() {
		super("ticket", "mod", "m.*", "<add/remove/list>", Msg.COMMAND_MOD.toString(), "XcraftTickets.Assignee");
	}

	@Override
	public boolean execute(XcraftPluginManager pManager, CommandSender sender, String[] args) {
		TicketManager manager = (TicketManager) pManager;
		File root;
		try {
			root = new File(XcraftTickets.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			root = root.getParentFile();
			File f = new File(root, "XcraftTickets/archive");

			Map<String, Integer> list = new HashMap<String, Integer>();

			for (File file : f.listFiles()) {
				int number = Integer.parseInt(file.getName().replace(".yml", ""));
				if (number < 0)
					continue;
				try {
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
					BufferedReader br = new BufferedReader(isr);
					// Check for BOM character.
					br.mark(1);
					int bom = br.read();
					if (bom != 65279) {
						br.reset();
					}
					String s;
					while ((s = br.readLine()) != null) {
						if (s.contains("CLOSE")) {
							String[] split = s.split(";");
							if (list.containsKey(split[1])) {
								list.put(split[1], list.get(split[1]) + 1);
							} else {
								list.put(split[1], 1);
							}
						}
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("Rest", 0);

			for (String entry : list.keySet()) {
				if (!manager.getAssignees().contains(entry.trim())) {
					map.put("Rest", map.get("Rest") + list.get(entry));
				} else {
					map.put(entry.trim(), list.get(entry));
				}
			}
			list = null;
			ValueComparator bvc = new ValueComparator(map);
			Map<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
			sorted_map.putAll(map);
			pManager.plugin.messenger.sendInfo(sender, "XcraftTickets Stats:", true);
			for (String key : sorted_map.keySet()) {
				pManager.plugin.messenger.sendInfo(sender, "    " + key + ": " + map.get(key), true);
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;
		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		@Override
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
}
