package nz.co.pearson.vuwexams;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import nz.co.pearson.vuwexams.fragments.ExamsFragment;
import nz.co.pearson.vuwexams.fragments.GradesFragment;
import nz.co.pearson.vuwexams.fragments.TimetableFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBar actionBar = null;
    private static Realm realm = null;
    private int currentFragment = 0;
    private static final String KEY_SELECTED_FRAGMENT = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(realm != null && !realm.isClosed()) {
            realm.close();
        }
        try {
            realm = Realm.getInstance(this);
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(new RealmConfiguration.Builder(this).build());
            realm = Realm.getInstance(this);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_grades);

        if(savedInstanceState != null) {
            switchFragment(savedInstanceState.getInt(KEY_SELECTED_FRAGMENT, 0));
        } else {
            switchFragment(0);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switchFragment(item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragment(int menuItem) {
        Fragment newFragment;
        switch(menuItem) {
            default:
            case R.id.nav_grades:
                newFragment = new GradesFragment();
                actionBar.setTitle("Grades");
                break;
            case R.id.nav_exams:
                newFragment = new ExamsFragment();
                actionBar.setTitle("Exams");
                break;
            case R.id.nav_timetable:
                newFragment = new TimetableFragment();
                actionBar.setTitle("Timetable");
                break;
            case R.id.nav_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return;
            case R.id.nav_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return;
            case R.id.nav_about:
                new AlertDialog.Builder(this).setPositiveButton(android.R.string.ok, null).setTitle("About").setMessage(getString(R.string.about_message)).show();
                return;
        }
        currentFragment = menuItem;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_FRAGMENT, currentFragment);
        super.onSaveInstanceState(outState);
    }

    public static Realm getRealm() {
        return(realm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
