package com.scouter.cobbleoutbreaks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scouter.cobbleoutbreaks.entity.OutbreakPortal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public class OutbreakRewards {


    public static Codec<OutbreakRewards> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("item_rewards", Collections.emptyList()).forGetter(i -> i.itemRewards),
                    Codec.INT.optionalFieldOf("experience_reward", 0).forGetter(e -> e.experienceReward)
            )
            .apply(inst, OutbreakRewards::new)
    );
    private List<Item> itemRewards;
    private int experienceReward;
    public OutbreakRewards(List<Item> itemRewards, int experienceReward){
        this.itemRewards = itemRewards;
        this.experienceReward = experienceReward;
    }

    public List<Item> getRewards() {
        return itemRewards;
    }

    public int getExperience() {
        return experienceReward;
    }

    public static OutbreakRewards getDefaultRewards(){
        return new OutbreakRewards(Collections.emptyList(), 0);
    }
}
