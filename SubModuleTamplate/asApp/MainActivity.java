package xxx.xxx.xxx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.hoody.annotation.router.Router;

@Router("config/test")
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.IS_APPLICATION) {

        }
        findViewById(R.id.buttonPanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
}