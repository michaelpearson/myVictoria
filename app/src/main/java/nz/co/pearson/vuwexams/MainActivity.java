package nz.co.pearson.vuwexams;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.net.URI;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import nz.co.pearson.vuwexams.fragments.ExamsFragment;
import nz.co.pearson.vuwexams.fragments.Grades;
import nz.co.pearson.vuwexams.fragments.TimetableFragment;
import nz.co.pearson.vuwexams.networking.ApiWorker;
import nz.co.pearson.vuwexams.networking.Course;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private int currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_grades);
        switchFragment();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switchFragment(item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragment() {
        switchFragment(0);
    }

    private void switchFragment(int menuItem) {
        currentFragment = menuItem;
        Fragment newFragment;
        switch(menuItem) {
            default:
            case R.id.nav_grades:
                newFragment = new Grades();
                break;
            case R.id.nav_exams:
                newFragment = new ExamsFragment();
                break;
            case R.id.nav_timetable:
                newFragment = new TimetableFragment();
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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();
    }
}