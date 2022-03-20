package me.greenem.ItemListGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class AcceptOrDeclineCommandTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		//if(cmd.getName().equalsIgnoreCase("add")) {
		List<String> list = new ArrayList<String>();
//		if (args.length == 1) {
//			list.add("accepted");
//			list.add("declined");
//			removeOther(list, args, 0);
//		}
		if (args.length == 1) {
			for (Material m : Material.values()) {
				if(!m.name().contains("legacy")) {
					list.add(m.name().toLowerCase());
				}
			}
			removeOther(list, args, 0);
		}
		return list;
		//}
		//return null;
	}
	
	private void removeOther(List<String> list, String[] args, int pos) {
		for (Iterator<String> it = list.iterator(); it.hasNext();) {
			String s = it.next();
			if (args[pos] != null && !s.startsWith(args[pos])) {
				it.remove();
			}
		}
	}
}