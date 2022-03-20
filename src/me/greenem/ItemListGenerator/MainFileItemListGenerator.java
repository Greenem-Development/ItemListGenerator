package me.greenem.ItemListGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class MainFileItemListGenerator extends JavaPlugin implements Listener {
	private boolean test = false;
	private int testLength = 5;
	
	private Logger log = getLogger();
	
	private ArrayList<String> allMaterials = new ArrayList<>();
	private ArrayList<String> acceptedMaterials = new ArrayList<>();
	private ArrayList<String> declinedMaterials = new ArrayList<>();
	public int currentPosotion = 0;
	
	private String nowName = "";
	
	private CommandSender sender;
	
	@Override
	public void onDisable() {
		log.info(ChatColor.RED + "ItemListGenerator plugin has been disabled.");
	}

	@Override
	public void onEnable() {
		log.info(ChatColor.GREEN + "ItemListGenerator plugin has been enabled!");
		getServer().getPluginManager().registerEvents(this, this);
		initMaterials();
		
		sender = Bukkit.getConsoleSender();
		
		getCommand("accept").setTabCompleter(new AcceptOrDeclineCommandTabCompleter());
		getCommand("decline").setTabCompleter(new AcceptOrDeclineCommandTabCompleter());
	}
	
	public void initMaterials() {
		acceptedMaterials.clear();
		declinedMaterials.clear();
		for (Material m : Material.values()) {
			if(!m.name().contains("LEGACY")) {
				allMaterials.add(m.name());
			}
			//log.info(m.name());
			//acceptedMaterials.add(m);
			//deniedMaterials.add(m);
		}
		Collections.sort(allMaterials);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("listgen")){
			if(sender instanceof ConsoleCommandSender) {
				acceptedMaterials.clear();
				declinedMaterials.clear();
				currentPosotion = 0;
				sendNextItem();
				this.sender = sender;
			}
		}
		else if(cmd.getName().equalsIgnoreCase("currentlist")){
			printResultOrDoNext(true);
		}
		else if (cmd.getName().equalsIgnoreCase("toindexlist")) {
			if (args.length > 0) {
				try {
					Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid index");
					return true;
				} catch (NullPointerException e) {
					sender.sendMessage("Invalid index");
					return true;
				}
				currentPosotion = Integer.parseInt(args[0])-1;
				printResultOrDoNext();
			}
		}
		else if(cmd.getName().equalsIgnoreCase("resetlist")){
			acceptedMaterials.clear();
			declinedMaterials.clear();
			sender.sendMessage(ChatColor.GREEN + "Cleared.");
		}
		else if(cmd.getName().equalsIgnoreCase("accept")){
			if(args.length==1) {
				if(allMaterials.contains(args[0].toUpperCase())) {
					if(acceptedMaterials.contains(args[0].toUpperCase())) {
						sender.sendMessage("This item is already accepted!");
						return true;
					}
					if(declinedMaterials.contains(args[0].toUpperCase())) declinedMaterials.remove(args[0].toUpperCase());
					if(!acceptedMaterials.contains(args[0].toUpperCase())) acceptedMaterials.add(args[0].toUpperCase());
					sender.sendMessage("Accepted " + ChatColor.YELLOW + args[0].toUpperCase() + ChatColor.RESET + "!");
					sendNextItem();
				}
			}
			else {
				sender.sendMessage("Wrong command usage");
				return true;
			}
		}
		else if(cmd.getName().equalsIgnoreCase("decline")){
			if(args.length==1) {
				if(allMaterials.contains(args[0].toUpperCase())) {
					if(declinedMaterials.contains(args[0].toUpperCase())) {
						sender.sendMessage("This item is already declined!");
						return true;
					}
					if(acceptedMaterials.contains(args[0].toUpperCase())) acceptedMaterials.remove(args[0].toUpperCase());
					if(!declinedMaterials.contains(args[0].toUpperCase())) declinedMaterials.add(args[0].toUpperCase());
					sender.sendMessage("Declined " + ChatColor.YELLOW + args[0].toUpperCase() + ChatColor.RESET + "!");
					sendNextItem();
				}
			}
			else {
				sender.sendMessage("Wrong command usage");
				return true;
			}
		}
		else if(cmd.getName().equalsIgnoreCase("acceptsimilar")){
			if(nowName.equals("")) {
				sender.sendMessage("There is no previous item!");
				return true;
			}
			int num = 0;
			if(args.length>0) {
				try {
					num = Integer.parseInt(args[0]) - 1;
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid index");
					return true;
				} catch (NullPointerException e) {
					sender.sendMessage("Invalid index");
					return true;
				}
			}
			ArrayList<String> newItems = new ArrayList<>();
			int i = 0;
			boolean t = false;
			for (i = currentPosotion; i < getAmount(); i++) {
				if(nowName.split("_")[num].equals(allMaterials.get(i).split("_")[num])){
					t = true;
					newItems.add(allMaterials.get(i));
				}
				else {
					if(t==true) {
						break;
					}
				}
			}
			if(i!=currentPosotion) {
				for (String s : newItems) {
					acceptedMaterials.add(s);
				}
				currentPosotion = i;
				printArrayList("New " + ChatColor.GREEN + "accepted" + ChatColor.RESET, newItems, false, 2, ChatColor.GOLD, ChatColor.WHITE);
				//log.info(i + ", " + currentPosotion + ", " + allMaterials.get(i) + ", " + allMaterials.get(currentPosotion));
				sendNextItem();
			}
			else {
				sender.sendMessage("There is nothing similar with word \"" + nowName.split("_")[num] + "\"");
			}
		}
		else if(cmd.getName().equalsIgnoreCase("declinesimilar")){
			if(nowName.equals("")) {
				sender.sendMessage("There is no previous item!");
				return true;
			}
			int num = 0;
			if(args.length>0) {
				try {
					num = Integer.parseInt(args[0]) - 1;
				} catch (NumberFormatException e) {
					sender.sendMessage("Invalid index");
					return true;
				} catch (NullPointerException e) {
					sender.sendMessage("Invalid index");
					return true;
				}
			}
			ArrayList<String> newItems = new ArrayList<>();
			int i = 0;
			boolean t = false;
			for (i = currentPosotion; i < getAmount(); i++) {
				if(nowName.split("_")[num].equals(allMaterials.get(i).split("_")[num])){
					t = true;
					newItems.add(allMaterials.get(i));
				}
				else {
					if(t==true) {
						break;
					}
				}
			}
			if(i!=currentPosotion) {
				for (String s : newItems) {
					declinedMaterials.add(s);
				}
				currentPosotion = i;
				printArrayList("New " + ChatColor.RED + "declined" + ChatColor.RESET, newItems, false, 2, ChatColor.GOLD, ChatColor.WHITE);
				sendNextItem();
			}
			else {
				sender.sendMessage("There is nothing similar with word \"" + nowName.split("_")[num] + "\"");
			}
		}
		return false;
	}
	
	public void sendNextItem() {
		if(currentPosotion<getAmount() && (acceptedMaterials.contains(allMaterials.get(currentPosotion)) || declinedMaterials.contains(allMaterials.get(currentPosotion)))) {
			sendNextItem();
			return;
		}
		Bukkit.getConsoleSender().sendMessage("(" + (currentPosotion+1) + "/" + getAmount() + ") " + allMaterials.get(currentPosotion));
		if(currentPosotion>=0) {
			nowName = allMaterials.get(currentPosotion);
		}
	}
	
	public Material[] a = {Material.AIR, Material.OBSIDIAN}; //example
	
	public void printArrayList(String name, ArrayList<String> list) {
		printArrayList(name, list, true, 0);
	}
	
	public void printArrayList(String name, ArrayList<String> list, boolean materialWord, int type) {
		printArrayList(name, list, materialWord, type, ChatColor.RESET);
	}
	
	public void printArrayList(String name, ArrayList<String> list, boolean materialWord, int type, ChatColor listColor) {
		printArrayList(name, list, materialWord, type, listColor, ChatColor.RESET);
	}
	
	public void printArrayList(String name, ArrayList<String> list, boolean materialWord, int type, ChatColor listColor, ChatColor nameColor) { // 0 = normal, 1 = only list, 2 = only list with name
		String s = "";
		if(type==0) { //or other later
			if(materialWord) {
				s += "public " + nameColor +"Material[] ";	
			}
			s += nameColor + name + ChatColor.RESET + " = {";
		}
		else if(type==2) {
			s += nameColor + name + ": " + ChatColor.RESET;
		}
		s += listColor;
		int i = 0;
		for (String m : list) {
			i++; //idk why here
			if(type==0) {
				s += "Material." + m;
			}
			else if(type==1 || type==2) {
				s += m;
			}
			//log.info(i + " " + getLen());
			//log.info(i + " < " + list.size() + " = " + (i<list.size()));
			if(i<list.size()) {
				s += ChatColor.RESET;
				s += ", ";
				s += listColor;
			}
		}
		
		s += ChatColor.RESET;
		
		if(type==0 || materialWord) {
			s += "};";
		}
		sender.sendMessage(s);
	}
	
	@EventHandler
	public void ChatEvt(ServerCommandEvent e) {
		//log.info("ServerCommandEvent, " + e.getCommand());
		String s = e.getCommand();
		if(s.equals("y") || s.equals("yes")) {
			if(!acceptedMaterials.contains(allMaterials.get(currentPosotion))) {
				acceptedMaterials.add(allMaterials.get(currentPosotion));
			}
			if(declinedMaterials.contains(allMaterials.get(currentPosotion))) {
				declinedMaterials.remove(allMaterials.get(currentPosotion));	
			}
			currentPosotion++;
			printResultOrDoNext();
		}
		if(s.equals("n") || s.equals("no")) {
			if(!declinedMaterials.contains(allMaterials.get(currentPosotion))) {
				declinedMaterials.add(allMaterials.get(currentPosotion));
			}
			if(acceptedMaterials.contains(allMaterials.get(currentPosotion))) {
				acceptedMaterials.remove(allMaterials.get(currentPosotion));	
			}
			currentPosotion++;
			printResultOrDoNext();
		}
	}
	
	public void printResultOrDoNext() {
		printResultOrDoNext(false);
	}
	
	public void printResultOrDoNext(boolean onlyPosition) {
		if(onlyPosition || currentPosotion>=getAmount()) {
			if(!onlyPosition) {
				sender.sendMessage("You have reached the end.");
			}
			sender.sendMessage("Accepted:");
			printArrayList("AcceptedMaterials", acceptedMaterials, true, 0, ChatColor.AQUA, ChatColor.WHITE);
			sender.sendMessage("Declined:");
			printArrayList("DeclinedMaterials", declinedMaterials, true, 0, ChatColor.AQUA, ChatColor.WHITE);
		}
		else {
			sendNextItem();
		}
	}
	
	private int getAmount() {
		if(test) {
			return testLength;
		}
		else{
			return allMaterials.size(); //or Material.values().length()?
		}
	}
}