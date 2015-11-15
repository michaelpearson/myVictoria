package nz.co.pearson.vuwexams.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.Course;

/**
 * Created by michael on 14/11/15.
 */
public class YearGradesFragment extends NamedFragment {
    public static final String KEY_YEAR = "year";
    private List<Course> courses;
    private ListView list;

    @Override
    public String getTitle() {
        return(String.valueOf(getYear()));
    }

    public int getYear() {
        return getArguments().getInt(KEY_YEAR, 0);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_year_grades, container, false);

        Realm realm = Realm.getInstance(getActivity());
        courses = new ArrayList<>(realm.where(Course.class).equalTo("year", getYear()).findAll());
        realm.close();

        list = (ListView)view.findViewById(R.id.course_list);
        list.setAdapter(new GradesAdapter());
        return(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_YEAR, getYear());
    }

    class GradesAdapter extends BaseAdapter {

        private View views[] = new View[courses.size()];

        @Override
        public int getCount() {
            return views.length;
        }

        @Override
        public Object getItem(int position) {
            return(courses.get(position));
        }

        @Override
        public long getItemId(int position) {
            return courses.get(position).getCourseCode().hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(views[position] != null) {
                return(views[position]);
            } else {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View v = views[position] = inflater.inflate(R.layout.item_course, parent, false);
                TextView courseCode = (TextView)v.findViewById(R.id.text_course_code);
                TextView courseDescription = (TextView)v.findViewById(R.id.text_course_name);
                TextView courseGrade = (TextView)v.findViewById(R.id.text_grade);

                courseCode.setText(courses.get(position).getCourseCode());
                courseDescription.setText(courses.get(position).getTitle());
                courseGrade.setText(courses.get(position).getLetterGrade());

                return views[position];
            }
        }
    }
}
