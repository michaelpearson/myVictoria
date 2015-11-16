package nz.co.pearson.vuwexams.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by michael on 16/11/15.
 */
public abstract class Model<T extends RealmObject> {

    protected List<T> data;
    private Realm realm;
    private boolean refreshing = false;
    private Context context;

    protected Model(Context context) {
        this.context = context;
        realm = Realm.getInstance(context);
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                fireListeners(Model.this);
            }
        });
        data = getQuery(realm).findAll();
    }

    public interface DataChangeListener {
        void onDataChanged(Model model);
        void refreshFailed();
    }

    private Set<DataChangeListener> observers = new HashSet<>(64);


    public List<T> getData() {
        return new ArrayList<>(data);
    }

    protected abstract RealmQuery<T> getQuery(Realm realm);

    protected abstract boolean refresh(Realm realm);

    public void refresh() {
        refreshing = true;
        new Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(context);
                fireListeners(refresh(realm), Model.this);
                realm.close();
            }
        }.start();
    }

    public void addChangeListener(DataChangeListener o) {
        observers.add(o);
    }

    public void removeChangeListener(DataChangeListener o) {
        observers.remove(o);
    }

    public void removeAllListeners() {
        observers.clear();
    }


    private void fireListeners(Model<T> model) {
        fireListeners(true, model);
    }

    private void fireListeners(boolean success) {
        fireListeners(true, null);
    }

    private void fireListeners(final boolean success, final Model<T> model) {
        refreshing = false;
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for(DataChangeListener o : observers) {
                    if(o != null) {
                        if(success) {
                            o.onDataChanged(model);
                        } else {
                            o.refreshFailed();
                        }
                    }
                }
            }
        });
    }

    public boolean isRefreshing() {
        return(refreshing);
    }
}
