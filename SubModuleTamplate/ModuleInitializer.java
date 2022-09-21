package xxx.xxx.xxx;

import android.content.Context;

import com.hoody.annotation.module.AbsModuleInitializer;
import com.hoody.annotation.module.Initializer;


@Initializer(priority = 2)
public class ModuleInitializer extends AbsModuleInitializer {

    @Override
    public void onInit(Context context) {
    }

    @Override
    public int getModuleId() {
        return BuildConfig.MODULE_ID;
    }
}
