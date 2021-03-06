package grimbot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import grimbot.data.SQLiteJDBC;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public final class Bot {
	
	public static Config config = null;
	public static SQLiteJDBC database = null;
	public static String prefix = "";
	public static JDABuilder jda;
	public static List<Plugin> plugins = new ArrayList<Plugin>();
	private static final String pluginPath = System.getProperty("user.dir") 
			+ File.separator + "target" + File.separator + "classes" 
			+ File.separator + "grimbot" + File.separator + "plugins";
	
	public Bot(Config c, SQLiteJDBC d) {
		config = c;
		database = d;
		prefix = config.getSetting("prefix", "!");
	}
	
	public void Connect() {
		jda = new JDABuilder(AccountType.BOT)
				.setToken(config.getSetting("token", ""))
				.setGame(Game.of(config.getSetting("game", "with Java."), 
						config.getSetting("gamelink", "https://github.com/marzipanic/GrimBot")))
	        	.setBulkDeleteSplittingEnabled(false)
	        	.setAutoReconnect(true);
		
		loadPlugins();
		ChatListener listener = new ChatListener(prefix);
		ChatLogger logger = new ChatLogger(config.getSetting("gdriveapikey", null));
		jda.addListener(listener).addListener(logger);
		connectBot();
	}
	
	private static void loadPlugins() {
		// Reference: http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
		System.out.println("\nLoading plugins...");
		File folder = new File(pluginPath);
		File[] files = folder.listFiles();
		
		
		// Using reflection to load plugins at runtime
		ClassLoader classLoader = Bot.class.getClassLoader();
		for (File file : files){
			if (file.isFile()) {
				try {
					String name = file.getName();
					Class pluginClass = classLoader.loadClass("grimbot.plugins." + name.substring(0, name.lastIndexOf('.')));
					Constructor pluginCon = pluginClass.getConstructor();
					Plugin plugin = (Plugin)pluginCon.newInstance();
					plugins.add(plugin);
					System.out.println("PLUGIN: " + name);
				} catch (ClassNotFoundException e) {
					System.out.println("ERROR: Class not found for " + file.getName());
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					System.out.println("ERROR: Constructor not found for " + file.getName());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					System.out.println("ERROR: Failed to invoke plugin " + file.getName());
					e.printStackTrace();
				} catch (InstantiationException e) {
					System.out.println("ERROR: Failed to instantiate plugin " + file.getName());
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					System.out.println("ERROR: Illegal access to " + file.getName());
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: Illegal argument in " + file.getName());
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void connectBot() {
		try {
			jda.buildAsync();
		} catch (LoginException e) {
			System.out.println("\nERROR: Login failed.");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("\nERROR: Illegal Argument when connecting bot.");
			e.printStackTrace();
		} catch (RateLimitedException e) {
        	System.out.println("\nERROR: Bot has been rate limited.");
        }
	}
}