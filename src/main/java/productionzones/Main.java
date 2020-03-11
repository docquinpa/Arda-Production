package productionzones;

@Mod(modid="ProductionZones", version="1.0.0", serverSideOnly = true, acceptableRemoteVersions="*")

public class Main
{
public static String MODID = "modid";
public static String VERSION = "version";

@EventHandler
public void preInit(FMLPreInitializationEvent e)
{
}

@EventHandler
public void init(FMLInitializationEvent e)
{
}

@EventHandler
public void postInit(FMLPostInitializationEvent e)
{
}

@EventHandler
public void serverStarting(FMLServerStartingEvent e){
	
	//Éxecuter au démarage du serveur
	e.registerServerCommand(new Commandes());
	
}
}
