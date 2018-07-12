package nowfloats.nfkeyboard.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import nowfloats.nfkeyboard.R;
import nowfloats.nfkeyboard.interface_contracts.SpellCheckerInterface;

public class TestMainActivity extends AppCompatActivity{

    // Used to load the 'native-lib' library on application startup.

    SpellCheckerManager spellCheckerManager;
    EditText query;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        query = findViewById(R.id.edit_query);
        spellCheckerManager = new SpellCheckerManager(this,getLifecycle(), new SpellCheckerInterface() {
            @Override
            public void onSuggestion(String[] text) {

            }
        });

        getLifecycle().addObserver(spellCheckerManager);
    }

    public void goClick(View v){
        spellCheckerManager.getSuggestions(query.getText().toString());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(spellCheckerManager);
    }
}