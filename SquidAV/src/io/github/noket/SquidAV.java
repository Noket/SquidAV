package io.github.noket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SquidAV extends JavaPlugin implements Listener{
	
	private List<OfflinePlayer> verifiedOPs;
	private Set<OfflinePlayer> unverifiedOPs;
	private FileConfiguration config;
	private BukkitTask generativeTask;
	private File file;
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		file = new File("plugins/SquidAV/config.yml");
		
		config = getConfig();
		config.addDefault("aggressive", true);
		config.addDefault("banviolators", false);

		
		//Find all unverified ops saved in the system

		unverifiedOPs = Bukkit.getOperators();
		verifiedOPs = new ArrayList<>();
		
		//Find all verified ops saved in the system
		@SuppressWarnings("unchecked")
		final List<String> uuidStringList = (List<String>) config.getList("verified");
		
		Iterator<String> uuidStringIterator;
		
		try{
		uuidStringIterator = uuidStringList.iterator();
		}
		catch(NullPointerException ex)
		{
			config.set("verified", Collections.emptyList());
			unverifiedOPs = new HashSet<>();
			uuidStringIterator = Collections.emptyIterator();
		}
		
		while(uuidStringIterator.hasNext())
		{
			String next = uuidStringIterator.next();
			
			//Dont want the loop to break
			try{
			UUID nextUUID = UUID.fromString(next);
			verifiedOPs.add(Bukkit.getOfflinePlayer(nextUUID));
			}
			catch(IllegalArgumentException | NullPointerException ex)
			{
				ex.printStackTrace();
				continue;
			}
		}
		
		//Remove any unverified ops
		
		//Save the config
		try {config.save(file);} catch (IOException e) {e.printStackTrace();}

		
		//Create persistent and random (have fun beating randomized runnables, hackers) runnables which detect hackers and ban them.
		final JavaPlugin plugin = this;

		generativeTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			
			@Override
			public void run(){

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
					
					@Override
					public void run(){
						
						Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
						Iterator<? extends Player> onlinePlayerIterator = onlinePlayers.iterator();
						Set<OfflinePlayer> oppedPlayers = new HashSet<>();
						
						//Find all opped players
						while(onlinePlayerIterator.hasNext())
						{
							Player next = onlinePlayerIterator.next();
							try{
								if(next.isOp())
								{
									oppedPlayers.add(Bukkit.getOfflinePlayer(next.getUniqueId()));
								}
							}
							catch(NullPointerException ex)
							{
								ex.printStackTrace();
								continue;
							}
						}
						
						//See if they're allowed to be opped.
						
						Iterator<OfflinePlayer> unverifiedPlayers = oppedPlayers.iterator();
						while(unverifiedPlayers.hasNext())
						{
							OfflinePlayer next = unverifiedPlayers.next();
							if(!verifiedOPs.isEmpty())
							{
								if(verifiedOPs.contains(next))
								{
									/*
									 * Exodus 12:13
									 * 
									 * The blood shall be a sign for you, on the houses where you are. 
									 * And when I see the blood, I will pass over you, and no plague will befall you 
									 * to destroy you, when I strike the land of Egypt.
									 */
									continue;
								}
								else{
									try{
										//System.out.println("Case 1");
										String playerName = next.getName();
										String hostString = next.getPlayer().getAddress().getHostString();
										next.getPlayer().setOp(false);
										Bukkit.getBanList(BanList.Type.NAME).addBan(next.getPlayer().getName(), "SquidAV: Banned for unauthorized possession of Server Operator.", null, null).save();
										next.getPlayer().kickPlayer("SquidAV: Banned for unauthorized possession of Server Operator.");
										Bukkit.banIP(hostString);
										Bukkit.broadcastMessage("["+ChatColor.BOLD+ChatColor.DARK_RED+"SquidAV"+ChatColor.RESET+"] "+playerName+" banned for malicious activity.");
									}
									catch(NullPointerException ex)
									{
										continue;
									}
								}
							}
							else{
								try{
									//System.out.println("Case 2");

									String playerName = next.getName();
									String hostString = next.getPlayer().getAddress().getHostString();
									next.getPlayer().setOp(false);
									Bukkit.getBanList(BanList.Type.NAME).addBan(next.getPlayer().getName(), "SquidAV: Banned for unauthorized possession of Server Operator.", null, null).save();
									next.getPlayer().kickPlayer("SquidAV: Banned for unauthorized possession of Server Operator.");
									Bukkit.banIP(hostString);
									Bukkit.broadcastMessage("["+ChatColor.BOLD+ChatColor.DARK_RED+"SquidAV"+ChatColor.RESET+"] "+playerName+" banned for malicious activity.");
								}
								catch(NullPointerException ex)
								{
									continue;
								}
							}
						}
						
					}
				}, getRandomDelay());
			}
		}, 0L, 100L);
	}
	
	@Override
	public void onLoad()
	{
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			if("verify".equals(command.getName()))
			{
				if(args.length > 0)
				{
					try{
						UUID uid = UUID.fromString(args[0]);
						String uidString = uid.toString();
						
						List<String> verifiedList = (List<String>) config.getList("verified");
						if(verifiedList.isEmpty())
						{
							verifiedOPs.add(Bukkit.getOfflinePlayer(uid));
							verifiedList = new ArrayList<>();
							verifiedList.add(uidString);
							config.set("verified",verifiedList);
							try {config.save(file);} catch (IOException e) {e.printStackTrace();}
						}
						else
						{
							verifiedOPs.add(Bukkit.getOfflinePlayer(uid));
							if(!verifiedList.contains(uid.toString()))
							{
								verifiedList.add(uidString);
								config.set("verified",verifiedList);
								try {config.save(file);} catch (IOException e) {e.printStackTrace();}
							}
							else{
								return true;
							}
						}
						return true;
					} catch (IllegalArgumentException ex)
					{
						// Do nothing.
					}
					
					
					try{
						Player player = Bukkit.getPlayer(args[0]);
						
						List<String> verifiedList = (List<String>) config.getList("verified");
						if(verifiedList.isEmpty())
						{
							verifiedOPs.add(Bukkit.getOfflinePlayer(player.getUniqueId()));
							verifiedList = new ArrayList<>();
							verifiedList.add(player.getUniqueId().toString());
							config.set("verified",verifiedList);
							try {config.save(file);} catch (IOException e) {e.printStackTrace();}
						}
						else{
							verifiedOPs.add(Bukkit.getOfflinePlayer(player.getUniqueId()));
							if(!verifiedList.contains(player.getUniqueId().toString()))
							{
								verifiedList.add(player.getUniqueId().toString());
								config.set("verified",verifiedList);
								try {config.save(file);} catch (IOException e) {e.printStackTrace();}
							}
							else{
								return true;
							}
						}
						
						return true;
					}
					catch(NullPointerException ex)
					{
						return false;
					}
				}
			}
		}
		return false;
	}
	
	/*
	 * Forgive me for usin' dis.
	 */
	@EventHandler
	public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event)
	{
		//System.out.println("I triggered, Preprocess Event!");
		
		Player player = event.getPlayer();
		if(player.isOp())
		{
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
			if(!verifiedOPs.contains(offlinePlayer))
			{
				try{
					//System.out.println("Case 3");

				String playerName = player.getName();
				String hostString = player.getAddress().getHostString();
				player.setOp(false);
				Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "SquidAV: Banned for unauthorized possession of Server Operator.", null, null).save();
				player.kickPlayer("SquidAV: Banned for unauthorized possession of Server Operator.");
				Bukkit.banIP(hostString);
				Bukkit.broadcastMessage("["+ChatColor.BOLD+ChatColor.DARK_RED+"SquidAV"+ChatColor.RESET+"] "+playerName+" banned for malicious activity.");
				}
				catch(NullPointerException ex)
				{
					ex.printStackTrace();
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void inventoryEvent(InventoryOpenEvent event)
	{
		//System.out.println("I triggered!");
		
		List<HumanEntity> viewerList = event.getViewers();
		try{
			Iterator<HumanEntity> viewerListIterator = viewerList.iterator();
			while(viewerListIterator.hasNext())
			{
				HumanEntity nextEntity = viewerListIterator.next();
				if(!getEntityIsVerified(nextEntity) && nextEntity.isOp()){
					UUID uuid = nextEntity.getUniqueId();
					Player playerToBan = Bukkit.getPlayer(uuid);
					try{
						//System.out.println("Case 4");

						String playerName = playerToBan.getName();
						String hostString = playerToBan.getAddress().getHostString();
						playerToBan.setOp(false);
						Bukkit.getBanList(BanList.Type.NAME).addBan(playerToBan.getName(), "SquidAV: Banned for unauthorized possession of Server Operator.", null, null).save();
						playerToBan.kickPlayer("SquidAV: Banned for unauthorized possession of Server Operator.");
						Bukkit.banIP(hostString);
						Bukkit.broadcastMessage("["+ChatColor.BOLD+ChatColor.DARK_RED+"SquidAV"+ChatColor.RESET+"] "+playerName+" banned for malicious activity.");

					}
					catch(NullPointerException ex)
					{
						//Gotta love how many nulls bukkit throws
						continue;
					}
				}
				else{
					continue;
				}
			}
		}
		catch(NullPointerException ex)
		{
			//Bukkit returned null, so no further need to investigate
			return;
		}
	}
	
	@EventHandler
	public void interactEvent(PlayerInteractEvent event)
	{
		//System.out.println("I triggered, PlayerInteractEvent!");

		Player player = event.getPlayer();
		
		if(!getEntityIsVerified(player) && player.isOp())
		{
			//System.out.println("I triggered, player is a cheater!");

			try{
				//System.out.println("Case 4");

			String playerName = player.getName();
			String hostString = player.getAddress().getHostString();
			player.setOp(false);
			Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "SquidAV: Banned for unauthorized possession of Server Operator.", null, null).save();
			player.kickPlayer("SquidAV: Banned for unauthorized possession of Server Operator.");
			Bukkit.banIP(hostString);
			Bukkit.broadcastMessage("["+ChatColor.BOLD+ChatColor.DARK_RED+"SquidAV"+ChatColor.RESET+"] "+playerName+" banned for malicious activity.");
			}
			catch(NullPointerException ex)
			{
				ex.printStackTrace();
				return;
			}
		}
	}
	
	private long getRandomDelay()
	{
		double rand = Math.random();
		//4 ticks max, 1 ticks min
		rand *= 98;
		//Cast it to make a round number
		return (long) rand;
	}
	
	public <K extends HumanEntity> boolean getEntityIsVerified(K entity)
	{
		if(entity instanceof Player)
		{
			try{
				OfflinePlayer player = Bukkit.getOfflinePlayer(entity.getUniqueId());
				if(verifiedOPs.contains(player))
				{
					return true;
				}
				else{
					return false;
				}
			}
			catch(NullPointerException ex)
			{
				//Sigh @ bukkit ..
				return false;
			}
		}
		else if(entity instanceof HumanEntity)
		{
			try{
				if(verifiedOPs.contains(Bukkit.getOfflinePlayer(entity.getUniqueId())))
				{
					return true;
				}
				else{
					return false;
				}
			}
			catch(NullPointerException ex)
			{
				//Sigh @ bukkit ..
				return false;
			}
		}
		return false;
	}
}
