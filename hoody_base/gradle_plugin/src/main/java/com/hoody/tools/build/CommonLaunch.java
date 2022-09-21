package com.hoody.tools.build;


import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.hoody.tools.build.transforms.InitializerTransform;
import com.hoody.tools.build.util.Logger;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class CommonLaunch implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger.init(project);
        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);
        Logger.i(project.getDisplayName() + " 开始使用 com.hoody.android.common: isApp = " + isApp);
        if (isApp) {
            AppExtension appExtension = project.getExtensions().getByType(AppExtension.class);
            appExtension.registerTransform(new InitializerTransform(project));
        }
    }

}
