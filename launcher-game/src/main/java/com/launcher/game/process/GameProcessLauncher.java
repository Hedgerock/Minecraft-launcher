package com.launcher.game.process;

import com.launcher.core.game.GameLaunchPlan;

public interface GameProcessLauncher {

    Process launch(GameLaunchPlan plan);

}
