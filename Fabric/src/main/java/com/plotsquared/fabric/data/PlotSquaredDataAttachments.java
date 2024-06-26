package com.plotsquared.fabric.data;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PlotSquaredDataAttachments {

    public static AttachmentType<Plot> PLOT_DATA;
    public static AttachmentType<GlobalPos> P2;
    public static AttachmentType<PlotId> SHULKER_PLOT;
    public static AttachmentType<Boolean> PS_CUSTOM_SPAWNED;
    public static AttachmentType<Boolean> KEEP;
    public static AttachmentType<Boolean> PS_TMP_TELEPORT;
    public static AttachmentType<List<Plot>> PLOT;

    static {
        PLOT_DATA = AttachmentRegistry.create(new ResourceLocation("plotsquared", "plot_data"));
        P2 = AttachmentRegistry.create(new ResourceLocation("plotsquared", "p2"));
        SHULKER_PLOT = AttachmentRegistry.create(new ResourceLocation("plotsquared", "shulkerPlot"));
        PS_CUSTOM_SPAWNED = AttachmentRegistry.create(new ResourceLocation("plotsquared", "ps_custom_spawned"));
        KEEP = AttachmentRegistry.create(new ResourceLocation("plotsquared", "keep"));
        PS_TMP_TELEPORT = AttachmentRegistry.create(new ResourceLocation("plotsquared", "ps_tmp_teleport"));
        PLOT = AttachmentRegistry.create(new ResourceLocation("plotsquared", "plot"));
    }



}
