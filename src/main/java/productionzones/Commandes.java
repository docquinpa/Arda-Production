package productionzones;

import java.util.ArrayList;
import java.util.List;

import ibxm.Player;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class Commandes implements ICommand {
	
	
			

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
		World w = sender.getEntityWorld();
		int cox = sender.getPlayerCoordinates().posX;
		int coy = sender.getPlayerCoordinates().posY;
		int coz = sender.getPlayerCoordinates().posZ;
		Block command = Block.getBlockById(137);
		
		
		
		
		
		if(arguments.length <= 0){
			throw new WrongUsageException(this.getCommandUsage(sender));
			
		}
		
		if(arguments[0].matches("ping")){
		}
		
		if(arguments[0].matches("createzone")){
			w.setBlock(coz, coy, coz, command);
			
			
			
		}
		if(arguments[0].matches("deletezone")){
		}
		if(arguments[0].matches("modifyzone")){
			if(arguments[1].matches("touchItem")){
			}
			if(arguments[1].matches("removeItem")){
			}
		}


	}

	@Override
	public List getCommandAliases() {
		List l = new ArrayList<String>();
		l.add("pdrz");
		return l;
		
	    
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		sender.canCommandSenderUseCommand(0, "");
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
		if (arguments.length == 1) {
		    return CommandBase.getListOfStringsMatchingLastWord(arguments, "createzone", "ping", "deletezone", "modifyzone");
		  } else if (arguments.length == 2) {
		    return CommandBase.getListOfStringsMatchingLastWord(arguments,"touchItem" , "removeItem" );
		  }
		
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		
		return false;
	}


}
