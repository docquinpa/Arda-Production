package productionzones;

import java.awt.Color;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cpw.mods.fml.common.FMLCommonHandler;
import ibxm.Player;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartEvent;
import net.minecraft.*;
public class Commandes extends CommandBase implements ICommand  {


	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName() {

		return "productionzones";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {

		return "commands.productionzones.usage";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] arguments) { 
		
		World w = sender.getEntityWorld(); //get the world from the sender send the command
		int cox = sender.getPlayerCoordinates().posX;
		int coy = sender.getPlayerCoordinates().posY;
		int coz = sender.getPlayerCoordinates().posZ;
		Block commandblock = Block.getBlockById(137); // command is a block with id 137 : minecraft:command_block
		EntityPlayerMP entityPlayerMP = null; 
		String Cplayer = w.getClosestPlayer(cox, coy, coz, 10).getDisplayName(); //get the clostest player from the sender
		String ESP = " "; //little fonction to set an space in string chains
			ProductionZonesDB zones = null;
			try {
				zones = new ProductionZonesDB("productionszones.json");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			MinecraftServer server = MinecraftServer.getServer();
			
			
			
			
		
		
		
		
		
		
		if(arguments.length <= 0 && arguments[1].isEmpty()){
			throw new WrongUsageException(this.getCommandUsage(sender));
			
		}
		/**======================================================================\
		 * Ping a Zone                                                            | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("ping")){
			Map<String , Long> ping = zones.ping(arguments[1]);
			
			
			
			
			System.out.println("The player : "+ Cplayer +  " has reclaim the zone : " + arguments[1]);
			
			for(Entry<String, Long> entry : ping.entrySet()){
				
				server.getCommandManager().executeCommand(sender, "give" + ESP  + Cplayer +  ESP + entry.getKey()+  ESP + entry.getValue());
			}
			try {
				zones.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		/**======================================================================\
		 * Create a zone                                                          | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("createZone")){
			
			
				if(zones.allZoneIds().contains(arguments[1])){
					sender.addChatMessage(new ChatComponentText("The zone :"+ESP+arguments[1]+ESP+ "already exist !"));
				}
				else{
					
					sender.addChatMessage(new ChatComponentText("The zone :"+ESP+arguments[1]+ESP+"was created ! "));
					w.setBlock(cox, coy, coz, commandblock);
					zones.createZone(arguments[1]);
					try {
				zones.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				}
			
		}
		
		/**======================================================================\
		 * Delete a Zone                                                          | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("deleteZone")){
				if(zones.allZoneIds().contains(arguments[1])){
					sender.addChatMessage(new ChatComponentText("The Zone :"+ESP+arguments[1]+ESP+"has been removed !"));
					zones.removeZone(arguments[1]);
				}
				else{
					sender.addChatMessage(new ChatComponentText("The Zone :"+ESP+arguments[1]+ESP+"does not exist !"));
				}
			try {
				zones.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			}
		

			
		/**======================================================================\
		 * Add an item to the Zone                                                | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("touchItem")){
					zones.touchItem(arguments[1],arguments[2], new TimeLapse(arguments[3]).getMillis()  );
					sender.addChatMessage(new ChatComponentText("You have added the item:"+ESP+arguments[2]+ESP+"to the zone"+ESP+arguments[1]+ESP+"!"+ESP+"It will be produced at a rate of 1 every :"+ESP+ new TimeLapse(arguments[3]).getMillis()/60000+ESP+"minutes !"));
				
				try {
					zones.save();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		/**======================================================================\
		 * Remove item from the Zone                                              | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("removeItem")){
				zones.removeItem(arguments[1], arguments[2]);
				sender.addChatMessage(new ChatComponentText("You have removed the item"+ESP+arguments[2]+ESP+"to the zone :"+ESP+arguments[1]+ESP+"!"));
			}
		
			
			/**======================================================================\
			 * Get all zone name                                                      | 
			 * ======================================================================/
			 */
				if(arguments[0].equalsIgnoreCase("list")){
					sender.addChatMessage(new ChatComponentText(zones.allZoneIds().toString()));	
				}
				try {
					zones.save();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			/**======================================================================\
			 * Get all zone info                                                      | 
			 * ======================================================================/
			 */
				if(arguments[0].equalsIgnoreCase("zoneInfo")){
					Map<String , Long> Info = zones.zoneInfo(arguments[1]);
					
					for(Entry<String, Long> entry : Info.entrySet()){
						sender.addChatMessage(new ChatComponentText("The zone :"+ESP+arguments[1]+ESP+"contains :"+ESP+entry.getKey()));
						
					}
					
					
				}
				
			/**======================================================================\
			 * Change the max time production                                         | 
			 * ======================================================================/
			 */
				if(arguments[0].equalsIgnoreCase("ChangeMaxProductionTime")){
					zones.changeMaxProdictionTime(new TimeLapse(arguments[1]).getMillis());
					sender.addChatMessage(new ChatComponentText("You have changed the maximum production time of the mod to"+ESP+new TimeLapse(arguments[1]).getMillis()/86400000+ESP+"days !"));
					try {
						zones.save();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				


	}
	
	/**======================================================================\
	 * Shortest command name                                                  | 
	 * ======================================================================/
	 */
		@Override
		public List getCommandAliases() {
		List l = new ArrayList<String>();
		l.add("prdz");
		return l;
		
	    
	}

	/**======================================================================\
	 * Set the permissions required to send the command                       | 
	 * ======================================================================/
	 */
		@Override
		public boolean canCommandSenderUseCommand(ICommandSender sender) {
		sender.canCommandSenderUseCommand(0, "");
		return true;
	}

	/**======================================================================\
	 * Auto completion with Tabulation                                        | 
	 * ======================================================================/
	 */
		@Override
		public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
		if (arguments.length == 1) {
		    return CommandBase.getListOfStringsMatchingLastWord(arguments, "createZone", "ping", "deleteZone", "touchItem" , "list" , "zoneInfo", "changeMaxProductionTime" , "removeItem");
		  } 
		
			return null;
	}
	/**======================================================================\
	 * Unknown method                                                          | 
	 * ======================================================================/
	 */
		@Override
		public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		
		return false;
	}


}

