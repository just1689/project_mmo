package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.BlockBrokenHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Mouse;


import java.io.IOException;
import java.util.*;

public class ListScreen extends GuiScreen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static final Style greenColor = XP.textStyle.get( "green" );
    private static TileButton exitButton;

    ScaledResolution sr = new ScaledResolution( Minecraft.getMinecraft() );
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY, accumulativeHeight, buttonsSize, buttonsLoaded, futureHeight, minCount, maxCount;
    private ListScrollPanel scrollPanel;
    private final EntityPlayer player;
    private final JType jType;
    private final double baseXp = FConfig.getConfig( "baseXp" );
    private ArrayList<ListButton> listButtons = new ArrayList<>();
    private UUID uuid;
    private ITextComponent title;
    private String type;

    public ListScreen(UUID uuid, ITextComponent titleIn, String type, JType jType, EntityPlayer player )
    {
        super();
        this.title = titleIn;
        this.player = player;
        this.jType = jType;
        this.uuid = uuid;
        this.type = type;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    public void initGui()
    {
        sr = new ScaledResolution( mc );
        ArrayList<String> keyWords = new ArrayList<>();
        keyWords.add( "helmet" );
        keyWords.add( "chestplate" );
        keyWords.add( "leggings" );
        keyWords.add( "boots" );
        keyWords.add( "pickaxe" );
        keyWords.add( "axe" );
        keyWords.add( "shovel" );
        keyWords.add( "hoe" );
        keyWords.add( "sword" );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;
        String skill;

        exitButton = new TileButton( 1337, x + boxWidth - 24, y - 8, 7, 0, "", JType.NONE, (button) ->
        {
            switch( jType )
            {
                case SKILLS:
                case STATS:
                case HISCORE:
                    Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, getTransComp( "pmmo.potato" ) ) );
                    break;

                default:
                    Minecraft.getMinecraft().displayGuiScreen( new GlossaryScreen( uuid, getTransComp( "pmmo.skills" ), false ) );
                    break;
            }
        });

        Map<String, Map<String, Double>> reqMap = JsonConfig.data.get( jType );
        Map<String, Map<String, Map<String, Double>>> reqMap2 = JsonConfig.data2.get( jType );

        ArrayList<ListButton> tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        switch( jType )      //How it's made: Buttons!
        {
            case REQ_BIOME:
            {
                Map<String, Map<String, Double>> bonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME );
                Map<String, Map<String, Double>> scaleMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER );
                List<String> biomesToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( bonusMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : bonusMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                if( scaleMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : scaleMap.entrySet() )
                    {
                        if( !biomesToAdd.contains( entry.getKey() ) )
                            biomesToAdd.add( entry.getKey() );
                    }
                }

                biomesToAdd.sort( Comparator.comparingInt( b -> getReqCount( b, JType.REQ_BIOME ) ) );

                for( String regKey : biomesToAdd )
                {
                    if ( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ) != null )
                    {
                        tempList.add( new ListButton( 1337, 0, 0, 3, 8, regKey, jType, "", ListButton::clickActionGlossary ) );
                    }
                }
            }
            break;

            case DIMENSION:
            {
                Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                if( veinBlacklist == null )
                    break;

                if( veinBlacklist.containsKey( "all_dimensions" ) )
                {
                    tempList.add( new ListButton( 1337, 0, 0, 3, 8, "all_dimensions", jType, "", ListButton::clickActionGlossary ) );
                }


                if( veinBlacklist.containsKey( "minecraft:overworld" ) )
                {
                    tempList.add( new ListButton( 1337, 0, 0, 3, 8, "minecraft:overworld", jType, "", ListButton::clickActionGlossary ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_nether" ) )
                {
                    tempList.add( new ListButton( 1337, 0, 0, 3, 8, "minecraft:the_nether", jType, "", ListButton::clickActionGlossary ) );
                }

                if( veinBlacklist.containsKey( "minecraft:the_end" ) )
                {
                    tempList.add( new ListButton( 1337, 0, 0, 3, 8, "minecraft:the_end", jType, "", ListButton::clickActionGlossary ) );
                }

//                for( Map.Entry<String, Map<String, Double>> entry : veinBlacklist.entrySet() )
//                {
//                    if ( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( entry.getKey() ) ) != null )
//                    {
//                        tempList.add( new ListButton( 1337, 0, 0, 3, 8, entry.getKey(), jType, "", ListButton::clickActionGlossary ) );
//                    }
//                }
                //COUT Dimensions
            }
            break;

            case REQ_KILL:
            {
                Map<String, Map<String, Double>> killXpMap = JsonConfig.data.get( JType.REQ_KILL );
                Map<String, Map<String, Double>> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP );
                ArrayList<String> mobsToAdd = new ArrayList<>();

                if( reqMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( killXpMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : killXpMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                if( rareDropMap != null )
                {
                    for( Map.Entry<String, Map<String, Double>> entry : rareDropMap.entrySet() )
                    {
                        if( !mobsToAdd.contains( entry.getKey() ) )
                            mobsToAdd.add( entry.getKey() );
                    }
                }

                for( String regKey : mobsToAdd )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
                    {
                        tempList.add( new ListButton( 1337, 0, 0, 3, 0, regKey, jType, "", ListButton::clickActionGlossary ) );
                    }
                }
            }
            break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 1337, 0, 0, 3, 0, entry.getKey(), jType, "", ListButton::clickActionGlossary ) );
                    }
                }
            }
            break;

            case FISH_ENCHANT_POOL:
            {
                if( reqMap == null )
                    break;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( ForgeRegistries.ENCHANTMENTS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                    {
                        tempList.add( new ListButton( 1337, 0, 0, 3, 25, entry.getKey(), jType, "", ListButton::clickActionGlossary ) );
                    }
                }
                break;
            }

            case SKILLS:
            {
                Set<String> skills = XP.getOfflineXpMap( uuid ).keySet();
                List<ListButton> buttonsToAdd = new ArrayList<>();
                listButtons.add( new ListButton( 1337, 0, 0, 3, 6, "totalLevel", jType, "", ListButton::clickActionSkills ) );
                for( String theSkill : skills )
                {
                    buttonsToAdd.add( new ListButton( 1337, 0, 0, 3, 6, theSkill.toString(), jType, "", ListButton::clickActionSkills ) );
                }
                buttonsToAdd.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( ((ListButton) b).regKey, uuid ) ).reversed() );
                listButtons.addAll( buttonsToAdd );
            }
            break;

            case HISCORE:
            {
                String theSkill = type;

                List<ListButton> buttonsToAdd = new ArrayList<>();

                for( Map.Entry<UUID, String> entry : XP.playerNames.entrySet() )
                {
                    buttonsToAdd.add( new ListButton( 1337, 0, 0, 3, 6, entry.getValue(), jType, "", ListButton::clickActionSkills ) );
                }
                if( type.equals( "totalLevel" ) )
                    buttonsToAdd.sort( Comparator.comparingDouble( b -> XP.getTotalLevelFromMap( XP.getOfflineXpMap( XP.playerUUIDs.get( ((ListButton) b).regKey ) ) ) ).reversed() );
                else
                    buttonsToAdd.sort( Comparator.comparingDouble( b -> XP.getOfflineXp( theSkill, XP.playerUUIDs.get( ((ListButton) b).regKey ) ) ).reversed() );
                listButtons.addAll( buttonsToAdd );
            }
            break;

            case SALVAGE:
            case SALVAGE_FROM:
            case TREASURE:
            case TREASURE_FROM:
            {
                if( reqMap2 == null )
                    break;
                for( Map.Entry<String, Map<String, Map<String, Double>>> salvageFromItemEntry : reqMap2.entrySet() )
                {
                    tempList.add( new ListButton( 1337, 0, 0, 3, 0, salvageFromItemEntry.getKey(), jType, "", ListButton::clickActionGlossary ) );
                }
            }
            break;

            default:
            {

                if( reqMap == null )
                    return;

                for( Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet() )
                {
                    if( XP.getItem( entry.getKey() ) != Items.AIR )
                    {
                        tempList.add( new ListButton( 1337, 0, 0, 3, 0, entry.getKey(), jType, "", ListButton::clickActionGlossary ) );
                    }
                }
            }
            break;
        }

        for( String keyWord : keyWords )
        {
            for( ListButton button : tempList)
            {
                if( button.regKey.contains( keyWord ) )
                {
                    if( !listButtons.contains(button) )
                        listButtons.add( button );
                }
            }
        }

        for( ListButton button : tempList)
        {
            if( !listButtons.contains( button ) )
                listButtons.add( button );
        }

        for( ListButton button : listButtons )
        {
            List<String> skillText = new ArrayList<>();
            List<String> scaleText = new ArrayList<>();
            List<String> effectText = new ArrayList<>();

            switch (jType)   //Individual Button Handling
            {
                case DIMENSION:
                {
                    Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST );
                    Map<String, Double> dimensionBonusMap = JsonConfig.data.get( JType.XP_BONUS_DIMENSION ).get(button.regKey);

                    if( veinBlacklist != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.veinBlacklist" ).setStyle( XP.textStyle.get( "red" ) ).getUnformattedText() );
                        for ( Map.Entry<String, Double> entry : veinBlacklist.get( button.regKey ).entrySet() )
                        {
                            button.text.add( new TextComponentString( " " + new ItemStack( XP.getItem( entry.getKey() ) ).getDisplayName() ).setStyle( XP.textStyle.get( "red" ) ).getUnformattedText() );
                        }
                    }

                    if ( dimensionBonusMap != null )
                    {
                        for (Map.Entry<String, Double> entry : dimensionBonusMap.entrySet())
                        {
                            if ( entry.getValue() > 0 )
                                skillText.add( new TextComponentString( " " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").getUnformattedText() ).setStyle(Skill.getSkillStyle(entry.getKey())).getUnformattedText());
                            if ( entry.getValue() < 0 )
                                skillText.add( new TextComponentString( " " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").getUnformattedText() ).setStyle(Skill.getSkillStyle(entry.getKey())).getUnformattedText());
                        }
                    }
                }
                break;

                case REQ_BIOME:
                {
                    if ( reqMap.containsKey( button.regKey ) )
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);

                    Map<String, Double> biomeBonusMap = JsonConfig.data.get( JType.XP_BONUS_BIOME ).get(button.regKey);
                    Map<String, Double> biomeMobMultiplierMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get(button.regKey);
                    Map<String, Double> biomeEffectsMap = JsonConfig.data.get( JType.BIOME_EFFECT_NEGATIVE ).get(button.regKey);

                    if ( biomeBonusMap != null )
                    {
                        for (Map.Entry<String, Double> entry : biomeBonusMap.entrySet()) {
                            if ( (double) entry.getValue() > 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").setStyle(Skill.getSkillStyle(entry.getKey())).getFormattedText());
                            if ( (double) entry.getValue() < 0 )
                                skillText.add(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").setStyle(Skill.getSkillStyle(entry.getKey())).getFormattedText());
                        }
                    }

                    if ( biomeMobMultiplierMap != null )
                    {
                        for ( Map.Entry<String, Double> entry : biomeMobMultiplierMap.entrySet() )
                        {
                            Style styleColor = new Style();

                            if ( entry.getValue() > 1 )
                                styleColor = XP.textStyle.get("red");
                            else if ( entry.getValue() < 1 )
                                styleColor = XP.textStyle.get("green");

                            switch ( entry.getKey() )
                            {
                                case "damageBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleDamage", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "hpBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleHp", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;

                                case "speedBonus":
                                    scaleText.add( " " + getTransComp("pmmo.enemyScaleSpeed", DP.dp( (double) entry.getValue() * 100) ).setStyle( styleColor ).getFormattedText() );
                                    break;
                            }
                        }
                    }

                    if ( biomeEffectsMap != null )
                    {
                        for ( Map.Entry<String, Double> entry : biomeEffectsMap.entrySet() )
                        {
                            if ( ForgeRegistries.POTIONS.containsKey( XP.getResLoc( entry.getKey() ) ) )
                            {
                                Potion effect = ForgeRegistries.POTIONS.getValue( XP.getResLoc( entry.getKey() ) );
                                if ( effect != null )
                                    effectText.add( " " + getTransComp( effect.getName() + " " + (int) ( entry.getValue() + 1) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                            }
                        }
                    }
                }
                break;

                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case INFO_SMELT:
                case INFO_COOK:
                case INFO_BREW:
                {
                    button.text.add( "" );
                    Map<String, Double> breakMap = JsonConfig.data.get( JType.REQ_BREAK ).get( button.regKey );
                    Map<String, Double> infoMap = XP.getJsonMap( button.regKey, jType );
                    List<String> infoText = new ArrayList<>();
                    String transKey = "pmmo." + jType.toString().replace( "info_", "" ) + "ExtraDrop";
                    double extraDroppedPerLevel = infoMap.get( "extraChance" ) / 100;
                    double extraDropped = XP.getExtraChance( player.getUniqueID(), button.regKey, jType, true ) / 100;

                    if ( extraDropped <= 0 )
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    else
                        infoText.add( getTransComp( transKey, DP.dp( extraDropped ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );

                    if ( extraDroppedPerLevel <= 0 )
                        infoText.add( getTransComp( "pmmo.extraPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("red") ).getFormattedText() );
                    else
                        infoText.add( getTransComp( "pmmo.extraPerLevel", DP.dpCustom( extraDroppedPerLevel, 4 ) ).setStyle( XP.textStyle.get("green") ).getFormattedText() );

                    if ( infoText.size() > 0 )
                        button.text.addAll( infoText );

                    if ( breakMap != null && ( jType.equals( JType.INFO_ORE ) || jType.equals( JType.INFO_LOG ) || jType.equals( JType.INFO_PLANT ) ) )
                    {
                        if ( XP.checkReq( player, button.regKey, JType.REQ_BREAK ) )
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( getTransComp( "pmmo.break" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                        addLevelsToButton( button, breakMap, player, false );
                    }
                }
                break;

                case XP_BONUS_WORN:
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), XP.checkReq( player, button.regKey, JType.REQ_WEAR ) );
                }
                break;

                case XP_BONUS_HELD:
                {
                    button.text.add( "" );
                    addPercentageToButton( button, reqMap.get( button.regKey ), true );
                }
                break;

                case XP_VALUE_BREED:
                case XP_VALUE_TAME:
                case XP_VALUE_SMELT:
                case XP_VALUE_COOK:
                case XP_VALUE_BREW:
                {
                    addXpToButton( button, reqMap.get( button.regKey ) );
                }
                break;

                case XP_VALUE_BREAK:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_BREAK, player );
                }
                break;

                case XP_VALUE_CRAFT:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_CRAFT, player );
                }
                break;

                case XP_VALUE_GROW:
                {
                    addXpToButton( button, reqMap.get( button.regKey ), JType.REQ_PLACE, player );
                }
                break;

                case FISH_ENCHANT_POOL:
                {
                    Map<String, Double> enchantMap = reqMap.get( button.regKey );

                    double fishLevel = Skill.getLevelDecimal( Skill.FISHING.toString(), player );

                    double levelReq = (double) enchantMap.get( "levelReq" );
                    double chancePerLevel = (double) enchantMap.get( "chancePerLevel" );
                    double maxChance = (double) enchantMap.get( "maxChance" );
                    double maxLevel = (int) (double) enchantMap.get( "maxLevel" );
                    double levelsPerTier = (double) enchantMap.get( "levelPerLevel" );
                    double maxLevelAvailable;
                    if( levelsPerTier == 0 )
                        maxLevelAvailable = maxLevel;
                    else
                        maxLevelAvailable = Math.floor( ( fishLevel - levelReq ) / levelsPerTier );
                    if( maxLevelAvailable < 0 )
                        maxLevelAvailable = 0;
                    if( maxLevelAvailable > maxLevel )
                        maxLevelAvailable = maxLevel;

                    double curChance = (fishLevel - levelReq) * chancePerLevel;
                    if( curChance > maxChance )
                        curChance = maxChance;
                    if( curChance < 0 )
                        curChance = 0;

                    button.unlocked = maxLevelAvailable > 0;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    button.text.add( "" );

                    button.text.add( " " + getTransComp( "pmmo.currentChance", DP.dpSoft( curChance ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.maxEnchantLevel", (int) maxLevelAvailable ).setStyle( color ).getFormattedText() );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).getFormattedText() );
                    if( maxLevel > 1 )
                        button.text.add( " " + getTransComp( "pmmo.levelsPerTier", DP.dpSoft( levelsPerTier ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.maxEnchantLevel", (int) maxLevel ).getFormattedText() );
                }
                break;

                case REQ_KILL:
                {
                    Map<String, Double> killXpMap = JsonConfig.data.get( JType.REQ_KILL ).get( button.regKey );
                    Map<String, Double> rareDropMap = JsonConfig.data.get( JType.MOB_RARE_DROP ).get(button.regKey);
                    button.unlocked = XP.checkReq( player, button.regKey, jType );
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    if ( reqMap.containsKey( button.regKey ) )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.toHarm" ).setStyle( color ).getFormattedText() );
                        addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                    }

                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo.xpValue" ).setStyle( color ).getFormattedText() );
                    if ( killXpMap != null )
                        addXpToButton( button, killXpMap, jType, player );
                    else
                    {
                        if( button.entity instanceof EntityAnimal )
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( FConfig.passiveMobHunterXp ) ).setStyle( color ).getFormattedText() );
                        else if( button.entity instanceof EntityMob)
                            button.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( FConfig.aggresiveMobSlayerXp ) ).setStyle( color ).getFormattedText() );
                    }

                    if ( rareDropMap != null )
                    {
                        button.text.add( "" );
                        button.text.add( getTransComp( "pmmo.rareDrops" ).setStyle( color ).getFormattedText() );
                        for( Map.Entry<String, Double> entry : rareDropMap.entrySet() )
                        {
                            button.text.add( " " + new TextComponentString( new ItemStack( XP.getItem( entry.getKey() ) ).getDisplayName() ).getFormattedText() + ": " + getTransComp( "pmmo.dropChance", DP.dpSoft( entry.getValue() ) ).setStyle( color ).getFormattedText() );
                        }
                    }
                }
                break;

                case FISH_POOL:
                {
                    Map<String, Double> fishPoolMap = reqMap.get( button.regKey );

                    double level = Skill.getLevelDecimal( Skill.FISHING.toString(), player );
                    double weight = XP.getWeight( (int) level, fishPoolMap );
                    button.unlocked = weight > 0;
                    Style color = XP.textStyle.get( button.unlocked ? "green" : "red" );

                    minCount = (int) (double) fishPoolMap.get( "minCount" );
                    maxCount = (int) (double) fishPoolMap.get( "maxCount" );

                    button.text.add( "" );
                    button.text.add( " " + getTransComp( "pmmo.currentWeight", weight ).setStyle( color ).getFormattedText() );

                    if ( minCount == maxCount )
                        button.text.add( " " + getTransComp( "pmmo.caughtAmount", minCount ).setStyle( color ).getFormattedText() );
                    else
                        button.text.add( " " + getTransComp( "pmmo.caughtAmountRange", minCount, maxCount ).setStyle( color ).getFormattedText() );

                    button.text.add( " " + getTransComp( "pmmo.xpEach", DP.dpSoft( (double) fishPoolMap.get("xp") ) ).setStyle( color ).getFormattedText() );

                    if ( button.itemStack.isItemEnchantable() )
                    {
                        if( (double) fishPoolMap.get( "enchantLevelReq" ) <= level && button.unlocked )
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                        else
                            button.text.add( " " + getTransComp( "pmmo.enchantLevelReq", DP.dpSoft( (double) fishPoolMap.get( "enchantLevelReq" ) ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                    }

                    button.text.add( "" );
//                    button.text.add( getTransComp( "pmmo.info" ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startWeight", DP.dpSoft( (double) fishPoolMap.get("startWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.startLevel", DP.dpSoft( (double) fishPoolMap.get("startLevel") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endWeight", DP.dpSoft( (double) fishPoolMap.get("endWeight") ) ).getFormattedText() );
                    button.text.add( " " + getTransComp( "pmmo.endLevel", DP.dpSoft( (double) fishPoolMap.get("endLevel") ) ).getFormattedText() );
                }
                break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                {
                    button.text.add( "" );
                    button.text.add( getTransComp( "pmmo." + jType.toString().replace( "req_", "" ) ).setStyle( XP.textStyle.get( XP.checkReq( player, button.regKey, jType ) ? "green" : "red" ) ).getFormattedText() );
                    addLevelsToButton( button, reqMap.get( button.regKey ), player, false );
                }
                break;

                case HISCORE:
                    skill = type;
                    String playerName = button.regKey;
                    UUID playerUUID = XP.playerUUIDs.get( playerName );
                    double xp;

                    if( type.equals( "totalLevel" ) )
                        button.text.add( new TextComponentString( "" + XP.getTotalLevelFromMap( XP.getOfflineXpMap( playerUUID ) ) ).getFormattedText() );
                    else
                    {
                        Map<String, Double> skillsMap;
                        Style color = XP.textStyle.get( "green" );
                        skillsMap = XP.getOfflineXpMap( playerUUID );
                        if( skillsMap.containsKey( skill ) )
                        {
                            xp = skillsMap.get( skill );
                            button.text.add( getTransComp( "pmmo.levelX", DP.dpSoft( XP.levelAtXpDecimal( xp ) ) ).setStyle( color ).getFormattedText() );
                            button.text.add( getTransComp( "pmmo.xpX", DP.dpSoft( xp ) ).setStyle( color ).getFormattedText() );
                        }
                    }
                    break;

                case SALVAGE:
                case SALVAGE_FROM:
                {
                    if( reqMap2 == null )
                        return;
                    int smithLevel = (int) Skill.getLevelDecimal( Skill.SMITHING.toString(), player );
                    String outputName;
                    double chance, levelReq, salvageMax, baseChance, chancePerLevel, maxChance, xpPerItem;
                    Map<String, Double> salvageToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>( reqMap2.get( button.regKey ).keySet() );
                    toItemsList.sort(Comparator.comparingInt( key -> (int) (double) reqMap2.get( button.regKey ).get( key ).get( "levelReq" ) ) );

                    for( String salvageToItemKey : toItemsList )
                    {
                        salvageToItemMap = reqMap2.get( button.regKey ).get( salvageToItemKey );
                        outputName       = new ItemStack( XP.getItem( salvageToItemKey ) ).getDisplayName();
                        levelReq         = salvageToItemMap.get( "levelReq" );
                        salvageMax       = salvageToItemMap.get( "salvageMax" );
                        baseChance       = salvageToItemMap.get( "baseChance" );
                        chancePerLevel   = salvageToItemMap.get( "chancePerLevel" );
                        maxChance        = salvageToItemMap.get( "maxChance" );
                        xpPerItem        = salvageToItemMap.get( "xpPerItem" );

                        chance = baseChance + ( chancePerLevel * ( smithLevel - levelReq ) );

                        if( chance < 0 )
                            chance = 0;
                        if( chance > maxChance )
                            chance = maxChance;

                        color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                        if( chance > 0 && smithLevel >= levelReq )
                            button.unlocked = true;

                        if( i++ == 0 )
                            button.text.add( new TextComponentTranslation( jType == JType.SALVAGE ? "pmmo.salvagesInto" : "pmmo.canBeSalvagedFrom" ).getUnformattedText() );
                        button.text.add( new TextComponentString( "____________________________" ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( new TextComponentString( getTransComp( jType == JType.SALVAGE ? "pmmo.valueValue" : "pmmo.valueFromValue", DP.dpSoft( salvageMax ), outputName ).getUnformattedText() ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.canBeSalvagedFromLevel", DP.dpSoft( levelReq ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.baseChance", DP.dpSoft( baseChance ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.chancePerLevel", DP.dpSoft( chancePerLevel ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.maxChancePerItem", DP.dpSoft( maxChance ) ).setStyle( color ).getUnformattedText() );
                    }
                }
                break;

                case TREASURE:
                case TREASURE_FROM:
                {
                    if( reqMap2 == null )
                        return;
                    int excavationLevel = (int) Skill.getLevelDecimal( Skill.EXCAVATION.toString(), player );
                    int startLevel, endLevel, minCount, maxCount;
                    String outputName;
                    double chance, startChance, endChance, xpPerItem;
                    Map<String, Double> treasureToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>( reqMap2.get( button.regKey ).keySet() );
                    toItemsList.sort(Comparator.comparingDouble( key -> BlockBrokenHandler.getTreasureItemChance( excavationLevel, reqMap2.get( button.regKey ).get( key ) ) ) );

                    for( String treasureToItemKey : toItemsList )
                    {
                        treasureToItemMap = reqMap2.get( button.regKey ).get( treasureToItemKey );
                        outputName      = new ItemStack( XP.getItem( treasureToItemKey ) ).getDisplayName();
                        startLevel      = (int) (double) treasureToItemMap.get( "startLevel" );
                        endLevel        = (int) (double) treasureToItemMap.get( "endLevel" );
                        startChance     = treasureToItemMap.get( "startChance" );
                        endChance       = treasureToItemMap.get( "endChance" );
                        xpPerItem       = treasureToItemMap.get( "xpPerItem" );
                        minCount        = (int) (double) treasureToItemMap.get( "minCount" );
                        maxCount        = (int) (double) treasureToItemMap.get( "maxCount" );

                        chance = BlockBrokenHandler.getTreasureItemChance( excavationLevel, treasureToItemMap );

                        color = XP.textStyle.get( chance > 0 ? "green" : "red" );

                        if( chance > 0  )
                            button.unlocked = true;

                        if( i++ == 0 )
                            button.text.add( new TextComponentTranslation( jType == JType.TREASURE ? "pmmo.containsTreasure" : "pmmo.treasureFrom" ).getUnformattedText() );
                        button.text.add( new TextComponentString( "____________________________" ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( new TextComponentString( outputName ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.xpPerItem", DP.dpSoft( xpPerItem ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.chancePerItem", DP.dpSoft( chance ) ).setStyle( color ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.minCount", minCount ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.maxCount", maxCount ).setStyle( color ).getUnformattedText() );
                        button.text.add( new TextComponentString( "" ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.startChance", startChance ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.startLevel", startLevel ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.endChance", endChance ).setStyle( color ).getUnformattedText() );
                        button.text.add( getTransComp( "pmmo.endLevel", endLevel ).setStyle( color ).getUnformattedText() );
                    }
                }
                break;

                case SKILLS:
                {
                    if( button.regKey.equals( "totalLevel" ) )
                    {
                        button.title = new TextComponentString( " " + getTransComp( "pmmo.totalLevel" ).getUnformattedText() ).getFormattedText();
                        button.text.add( new TextComponentString( "" + XP.getTotalLevelFromMap( XP.getOfflineXpMap( uuid ) ) ).getFormattedText() );
                    }
                    else
                    {
                        skill = button.regKey;

                        double curXp = XP.getOfflineXp( skill, uuid );
                        double nextXp = XP.xpAtLevel( XP.levelAtXp( curXp ) + 1 );

                        button.title = getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + button.regKey ), DP.dpSoft( XP.levelAtXpDecimal( curXp ) ) ).setStyle( Skill.getSkillStyle( button.regKey ) ).getFormattedText();

                        button.text.add( new TextComponentString( " " + getTransComp( "pmmo.currentXp", DP.dpSoft( curXp ) ).getFormattedText() ).getFormattedText() );
                        if( Skill.getLevel( skill, player ) != FConfig.getConfig( "maxLevel" ) )
                        {
                            button.text.add( new TextComponentString( " " + getTransComp( "pmmo.nextLevelXp", DP.dpSoft( nextXp ) ).getFormattedText() ).getFormattedText() );
                            button.text.add( new TextComponentString( " " + getTransComp( "pmmo.RemainderXp", DP.dpSoft( nextXp - curXp ) ).getFormattedText() ).getFormattedText() );
                        }
                    }
                }
                break;

                default:
                    break;
            }

            if( skillText.size() > 0 )
            {
                button.text.add( "" );
                skillText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.xpModifiers" ).getFormattedText() );
                button.text.addAll( skillText );
            }

            if( scaleText.size() > 0 )
            {
                if( skillText.size() > 0 )
                    button.text.add( "" );

                scaleText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.enemyScaling" ).getFormattedText() );
                button.text.addAll( scaleText );
            }

            if( effectText.size() > 0 )
            {
                if( skillText.size() > 0 || scaleText.size() > 0 )
                    button.text.add( "" );

                effectText.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );
                button.text.add( getTransComp( "pmmo.biomeEffects" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
                button.text.addAll( effectText );
            }

            switch( jType )  //Unlock/Lock buttons if necessary
            {
                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case XP_VALUE_BREAK:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_BREAK );
                    break;

                case XP_BONUS_WORN:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_WEAR );
                    break;

                case FISH_POOL:

                    break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                case REQ_BIOME:
                    button.unlocked = XP.checkReq( player, button.regKey, jType );
                    break;

                case XP_VALUE_CRAFT:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_CRAFT );
                    break;

                case XP_VALUE_GROW:
                    button.unlocked = XP.checkReq( player, button.regKey, JType.REQ_PLACE );
                    break;

                default:
                    break;
            }
//            children.add( button );
        }

        //SORT BUTTONS

        switch( jType )
        {
            case INFO_ORE:
            case INFO_LOG:
            case INFO_PLANT:
            case XP_VALUE_BREAK:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, JType.REQ_BREAK ) ) );
                break;

            case REQ_WEAR:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, JType.REQ_WEAR ) ) );
                break;

            case SALVAGE:
            case SALVAGE_FROM:
                listButtons.sort( Comparator.comparingDouble( b -> (double) getLowestSalvageReq( reqMap2.get( b.regKey ) ) ) );
                break;

            default:
                listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.regKey, jType ) ) );
                break;
        }

        switch( jType ) //default buttons
        {
            case XP_VALUE_BREED:
                ListButton otherAnimalsBreedButton = new ListButton( 1337, 0, 0, 3, 20, "pmmo.otherAnimals", jType, "", ListButton::clickActionGlossary );
                otherAnimalsBreedButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.farming" ), DP.dpSoft( FConfig.defaultBreedingXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsBreedButton );
                break;

            case XP_VALUE_TAME:
            {
                ListButton otherAnimalsTameButton = new ListButton( 1337, 0, 0, 3, 21, "pmmo.otherAnimals", jType, "", ListButton::clickActionGlossary );
                otherAnimalsTameButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.taming" ), DP.dpSoft( FConfig.defaultTamingXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAnimalsTameButton );
            }
            break;

            case REQ_KILL:
            {
                ListButton otherAggresiveMobsButton = new ListButton( 1337, 0, 0, 3, 26, "pmmo.otherAggresiveMobs", jType, "", ListButton::clickActionGlossary );
                otherAggresiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.slayer" ), DP.dpSoft( FConfig.aggresiveMobSlayerXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherAggresiveMobsButton );

                ListButton otherPassiveMobsButton = new ListButton( 1337, 0, 0, 3, 26, "pmmo.otherPassiveMobs", jType, "", ListButton::clickActionGlossary );
                otherPassiveMobsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.hunter" ), DP.dpSoft( FConfig.passiveMobHunterXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherPassiveMobsButton );
            }
            break;

            case XP_VALUE_CRAFT:
            {
                ListButton otherCraftsButton = new ListButton( 1337, 0, 0, 3, 22, "pmmo.otherCrafts", jType, "", ListButton::clickActionGlossary );
                otherCraftsButton.text.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo.crafting" ), DP.dpSoft( FConfig.defaultCraftingXp ) ).setStyle( greenColor ).getFormattedText() );
                listButtons.add( otherCraftsButton );
            }
            break;
        }
        scrollPanel = new ListScrollPanel( Minecraft.getMinecraft(), boxWidth - 40, boxHeight - 21, scrollY, scrollX, jType, player, listButtons );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        addButton( exitButton );
    }

    private static void addLevelsToButton( ListButton button, Map<String, Double> map, EntityPlayer player, boolean ignoreReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            if( !ignoreReq && Skill.getLevelDecimal( inEntry.getKey(), player ) < inEntry.getValue() )
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Double> map )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addXpToButton( ListButton button, Map<String, Double> map, JType jType, EntityPlayer player )
    {
        List<String> xpToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            xpToAdd.add( " " + getTransComp( "pmmo.xpDisplay", getTransComp( "pmmo." + inEntry.getKey() ), DP.dpSoft( (double) inEntry.getValue() ) ).setStyle( XP.textStyle.get( XP.checkReq( player, button.regKey, jType ) ? "green" : "red" ) ).getFormattedText() );
        }

        xpToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( xpToAdd );
    }

    private static void addPercentageToButton( ListButton button, Map<String, Double> map, boolean metReq )
    {
        List<String> levelsToAdd = new ArrayList<>();

        for( Map.Entry<String, Double> inEntry : map.entrySet() )
        {
            double value = inEntry.getValue();

            if( metReq )
            {
                if( value > 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), "+" + value + "%" ).setStyle( XP.textStyle.get( "green" ) ).getFormattedText() );
                else if( value < 0 )
                    levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
            }
            else
                levelsToAdd.add( " " + getTransComp( "pmmo.levelDisplay", getTransComp( "pmmo." + inEntry.getKey() ), value + "%" ).setStyle( XP.textStyle.get( "red" ) ).getFormattedText() );
        }

        levelsToAdd.sort( Comparator.comparingInt(ListScreen::getTextInt).reversed() );

        button.text.addAll( levelsToAdd );
    }

    private static int getTextInt( String comp )
    {
        String number = comp.replaceAll("\\D+","");

        if( number.length() > 0 && !Double.isNaN( Double.parseDouble( number ) ) )
            return (int) Double.parseDouble( number );
        else
            return 0;
    }

    private static double getTextDouble( String comp )
    {
        String number = comp.replaceAll("\\D+","");

        if( number.length() > 0 && !Double.isNaN( Double.parseDouble( number ) ) )
            return Double.parseDouble( number );
        else
            return 0;
    }

    private static int getReqCount( String regKey, JType jType )
    {
        Map<String, Double> map = XP.getJsonMap( regKey, jType );

        if( map == null )
            return 0;
        else
            return map.size();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );

        if( jType.equals( JType.SKILLS ) )
            title = getTransComp( "pmmo.playerStats", XP.playerNames.get( uuid ) );
        else if( jType.equals( JType.HISCORE ) )
            title = getTransComp( "pmmo.skillHiscores", getTransComp( "pmmo." + type ) ).setStyle( Skill.getSkillStyle( type ) );

        if( fontRenderer.getStringWidth( title.getFormattedText() ) > 220 )
            drawCenteredString( fontRenderer, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( fontRenderer, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.drawScreen( mouseX, mouseY, partialTicks );

        accumulativeHeight = 0;
        buttonsSize = listButtons.size();

        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.pushAttrib();
        for( ListButton button : listButtons )
        {
            buttonX = mouseX - button.x;
            buttonY = mouseY - button.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                if( jType.equals( JType.REQ_BIOME ) || jType.equals( JType.REQ_KILL ) || jType.equals( JType.XP_VALUE_BREED ) || jType.equals( JType.XP_VALUE_TAME ) || jType.equals( JType.DIMENSION ) || jType.equals( JType.FISH_ENCHANT_POOL ) || jType.equals( JType.SKILLS ) || jType.equals( JType.HISCORE ) || button.regKey.equals( "pmmo.otherCrafts" ) )
                    drawHoveringText( button.title, mouseX, mouseY );
                else if( button.itemStack != null )
                    renderToolTip( button.itemStack, mouseX, mouseY );
            }

            accumulativeHeight += button.getHeight();
        }
        GlStateManager.popAttrib();
        GlStateManager.enableBlend();

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
    }

    public static double getLowestSalvageReq( Map<String, Map<String, Double>> map )
    {
        double lowestReq = FConfig.getConfig( "maxLevel" );

        for( Map.Entry<String, Map<String, Double>> entry : map.entrySet() )
        {
            lowestReq = Math.min( lowestReq, (int) (double) entry.getValue().get( "levelReq" ) );
        }

        return lowestReq;
    }

    @Override
    public void drawBackground(int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );

        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

//    @Override
//    public boolean mouseScrolled( int mouseX, int mouseY, double scroll )
//    {
//        accumulativeHeight = 0;
//        for( ListButton listButton : listButtons )
//        {
//            accumulativeHeight += listButton.getHeight();
//            if( accumulativeHeight > scrollPanel.getBottom() - scrollPanel.getTop() )
//            {
//                scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
//                break;
//            }
//        }
//        return super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        scrollPanel.scroll( Mouse.getEventDWheel() );
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return;
        }

        for( ListButton a : listButtons )
        {
            int buttonX = mouseX - a.x;
            int buttonY = mouseY- a.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                a.onPress();
            }
        }
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased( int mouseX, int mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        scrollPanel.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }

    public static TextComponentTranslation getTransComp( String translationKey, Object... args )
    {
        return new TextComponentTranslation( translationKey, args );
    }
}