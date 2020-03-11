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
import sun.security.krb5.internal.crypto.crc32;
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
				zones = new ProductionZonesDB("fichier.json");
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
			
			
			
			
			System.out.println("Le joueur : "+ Cplayer +  "a récupérer la zone : " + arguments[1]);
			
			for(Entry<String, Long> entry : ping.entrySet()){
				
				server.getCommandManager().executeCommand(sender, "give" + ESP  + Cplayer +  ESP + entry.getKey()+  ESP + entry.getValue() );
				try {
					zones.save();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
		}
		
		/**======================================================================\
		 * Create a zone                                                          | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("createZone")){
			
			
				if(zones.allZoneIds().contains(arguments[1])){
					sender.addChatMessage(new ChatComponentText("La zone"+ESP+arguments[1]+ESP+ "existe déjà !"));
				}
				else{
					
					sender.addChatMessage(new ChatComponentText("La zone"+ESP+arguments[1]+ESP+"a été crée ! "));
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
			zones.removeZone(arguments[1]);		
			try {
				zones.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			}
		
		/**======================================================================\
		 * Modify a Zone                                                          | 
		 * ======================================================================/
		 */
			if(arguments[0].equalsIgnoreCase("modifyZone")){
			
			/**======================================================================\
			 * Add an item to the Zone                                                | 
			 * ======================================================================/
			 */
				if(arguments[1].equalsIgnoreCase("touchItem")){
					zones.touchItem( (arguments[3]), Long.parseLong(arguments[4]) ,arguments[2]);
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
				if(arguments[1].equalsIgnoreCase("removeItem")){
					zones.removeItem(arguments[2], arguments[3]);
					try {
						zones.save();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				
			}
		}
			
			/**======================================================================\
			 * Get all zone name                                                      | 
			 * ======================================================================/
			 */
				if(arguments[0].equalsIgnoreCase("list")){
					sender.addChatMessage(new ChatComponentText(zones.allZoneIds().toString()));
					
				}
				

			/**======================================================================\
			 * Get all zone info                                                      | 
			 * ======================================================================/
			 */
				if(arguments[0].equalsIgnoreCase("zoneInfo")){
					Map<String , Long> Info = zones.zoneInfo(arguments[1]);
					
					for(Entry<String, Long> entry : Info.entrySet()){
						sender.addChatMessage(new ChatComponentText("La zone"+ESP+arguments[1]+ESP+"contient :"+ESP+entry.getKey()));
						
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
		    return CommandBase.getListOfStringsMatchingLastWord(arguments, "createZone", "ping", "deleteZone", "modifyZone" , "list" , "zoneInfo");
		  } else if (arguments.length == 2) {
		    return CommandBase.getListOfStringsMatchingLastWord(arguments,"touchItem" , "removeItem");
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

