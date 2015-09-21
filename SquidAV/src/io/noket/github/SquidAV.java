package io.noket.github;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SquidAV extends JavaPlugin{
	
	private List<OfflinePlayer> verifiedOPs;
	private Set<OfflinePlayer> unverifiedOPs;
	private FileConfiguration config;
	private BukkitTask generativeTask;
	private File file;
	
	public SquidAV()
	{		
		file = new File("plugins/SquidAV/config.yml");
		
		config = getConfig();
		config.addDefault("aggressive", true);
		config.addDefault("banviolators", false);
		
		unverifiedOPs = Bukkit.getOperators();
		
		final JavaPlugin plugin = this;
		final List<String> uuidStringList = (List<String>) config.getList("verified");
		final String uuidString;
		final List<String> uuidList = uuidStringList.iterator().forEachRemaining(null);
		
		try {config.save(file);} catch (IOException e) {e.printStackTrace();}
		
		generativeTask = Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			
			@Override
			public void run(){
				
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
					
					@Override
					public void run(){
						
						
						
					}
				}, getRandomDelay());
			}
		}, 100L);
	}
	
	private byte getRandomDelay()
	{
		double rand = Math.random();
		rand *= 5;
		return (byte) rand;
	}
	
}
