package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nz.co.pearson.vuwexams.R;

/**
 * Created by michael on 14/11/15.
 */
public class ExamsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return(inflater.inflate(R.layout.fragment_exams, container, false));
    }
}
