package com.gendeathrow.skills.common;

import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.gendeathrow.skills.common.crafting.RecipeWrapper;
import com.gendeathrow.skills.common.skill.SkillTrackerData;
import com.gendeathrow.skills.common.stat.StatTrackerData;
import com.gendeathrow.skills.core.Skillz;
import com.gendeathrow.skills.entity.extended.EntityAnimalExt;
import com.gendeathrow.skills.entity.extended.EntityMobsExt;
import com.gendeathrow.skills.network.PacketDispatcher;
import com.gendeathrow.skills.network.client.SyncPlayersSkillPropsMessage;
import com.gendeathrow.skills.network.client.SyncPlayersStatsPropsMessage;
import com.gendeathrow.skills.utils.RecipeHelper;

public class EventHandler
{
	@SubscribeEvent
	public void onPlayerEnterWorld(EntityJoinWorldEvent event) throws InstantiationException, ReflectiveOperationException, Exception, Throwable
	{
		if (event.entity instanceof EntityPlayerMP) {
			Skillz.logger.info("Player joined world, sending extended properties to client");
			PacketDispatcher.sendTo(new SyncPlayersSkillPropsMessage((EntityPlayer) event.entity), (EntityPlayerMP) event.entity);
			PacketDispatcher.sendTo(new SyncPlayersStatsPropsMessage((EntityPlayer) event.entity), (EntityPlayerMP) event.entity);
		}
	
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		SkillTrackerData tracker = SkillTrackerData.get(event.entityPlayer);
		RecipeWrapper recipe = RecipeHelper.getWrappedRecipefromItemStack(event.itemStack);
		
		if(tracker != null && recipe != null)
		{
			if(tracker.GetSkillByID(recipe.getSkill()).getSkillLevel() >= recipe.getDifficulty())
			{
				event.toolTip.add(new ChatComponentTranslation(EnumChatFormatting.YELLOW+"You can build this!").getUnformattedText());
			}else {
				event.toolTip.add(new ChatComponentTranslation(EnumChatFormatting.RED+"Need: "+ tracker.GetSkillByID(recipe.getSkill()).getLocName() +" skill of "+ recipe.getDifficulty()).getUnformattedText());
				
			}
		}
	}
	
	@SubscribeEvent
	public void onClonePlayer(PlayerEvent.Clone event) {
		Skillz.logger.info("Cloning player extended properties");
		SkillTrackerData.get(event.entityPlayer).copy(SkillTrackerData.get(event.original));
		StatTrackerData.get(event.entityPlayer).copy(StatTrackerData.get(event.original));
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void open (PlayerOpenContainerEvent event)
	{
	}
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) throws InstantiationException, ReflectiveOperationException, Exception, Throwable {
	
		if (event.entity instanceof EntityPlayer) 
		{
			if (SkillTrackerData.get((EntityPlayer) event.entity) == null) 
			{
				Skillz.logger.info("Registering skill properties for player");
				SkillTrackerData.register((EntityPlayer) event.entity);
			}
			
			if (StatTrackerData.get((EntityPlayer) event.entity) == null)
			{
				Skillz.logger.info("Registering stat properties for player");
				StatTrackerData.register((EntityPlayer) event.entity);				
			}
			
		}
		else if(event.entity instanceof EntityMob)
		{
			EntityMobsExt.register((EntityMob) event.entity);
		}
		else if(event.entity instanceof EntityAnimal)
		{
			EntityAnimalExt.register((EntityAnimal) event.entity);
		}		
	}
	
	private void onEvent(Entity entity, Object event)
	{
		if(entity instanceof EntityPlayer)
		{
			SkillTrackerData tracker = SkillTrackerData.get((EntityPlayer)entity);			
			if(tracker != null)	tracker.sendEvent(entity,event);
		}	
	}
	
	@SubscribeEvent
	public void onAnvilRepair(AnvilRepairEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onBoneMealUse(BonemealEvent event)
	{
		onEvent(event.entityPlayer, event);
	}
	
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event)
	{
		onEvent(event.entityPlayer, event);
	}
	
	@SubscribeEvent
	public void onInteractWitEntity(EntityInteractEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onHarvest(PlayerEvent.HarvestCheck event)
	{
		onEvent(event.entity, event);
	}
	
	@SubscribeEvent
	public void BreakSpeed(PlayerEvent.BreakSpeed event)
	{
		onEvent(event.entity, event);
	}
	
	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event)
	{
		if(event.entityPlayer != null)
		{
			onEvent(event.entityPlayer, event);
		}
	}
	
	@SubscribeEvent
	public void onItemUse(PlayerUseItemEvent.Start event)
	{
		if(event.entityPlayer != null)
		{
			onEvent(event.entityPlayer, event);
		}
	}
	
	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event)
	{

		if(event.source.getSourceOfDamage() instanceof EntityArrow)
		{
			onEvent(((EntityArrow)event.source.getSourceOfDamage()).shootingEntity, event);
		}
		else if(event.entity instanceof EntityPlayer)
		{
			onEvent(event.entity, event);
		}
		
	}
	
	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event)
	{

		if(event.source.getEntity() != null && event.source.getEntity() instanceof EntityPlayer)
		{
			onEvent(event.source.getEntity(), event);
		}
	}
	
	@SubscribeEvent
	public void onFall(LivingFallEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onPotionBrewPre(PotionBrewEvent event)
	{
		System.out.println("brew pre");
	}
	
	@SubscribeEvent
	public void onPotionBrewPost(PotionBrewEvent.Post event)
	{
		System.out.println("brew posts");
	}
	
	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		if(event.placedBlock.getBlock() instanceof BlockEnchantmentTable)
		{
			//event.setCanceled(true);
			
			//event.world.setBlockState(event.pos, new IBlockState())
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		onEvent(event.getPlayer(), event);
	}
	
	@SubscribeEvent
	public void onBlockHarvest(BlockEvent.HarvestDropsEvent event)
	{
		onEvent(event.harvester, event);
	}
	
	@SubscribeEvent
	public void onBlockPlaced(BlockEvent.PlaceEvent event) 
	{
		
	}
	
	
	@SubscribeEvent
	public void onHoeEvent(UseHoeEvent event)
	{
		onEvent(event.entityPlayer, event);
	}
	
	@SubscribeEvent
	public void EntityItemPickupEvent(EntityItemPickupEvent event)
	{
	}
	
	@SubscribeEvent
	public void onCrafted(ItemCraftedEvent event)
	{
		onEvent(event.player, event);
	}
	

	@SubscribeEvent
	public void onSmelt(ItemSmeltedEvent event)
	{
		onEvent(event.player, event);
	}

}
