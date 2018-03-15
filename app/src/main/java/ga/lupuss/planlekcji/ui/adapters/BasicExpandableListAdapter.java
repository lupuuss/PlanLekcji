package ga.lupuss.planlekcji.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ga.lupuss.planlekcji.R;

public final class BasicExpandableListAdapter extends BaseExpandableListAdapter {

    private List<String> listDataHeader;

    private Map<String, List<String>> listDataChild;

    private Context context;

    public BasicExpandableListAdapter(@NonNull Context context,
                                      @NonNull List<String> listDataHeader,
                                      @NonNull Map<String, List<String>> listDataChild) {

        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
        this.context = context;
    }

    public static BasicExpandableListAdapter empty(@NonNull Context context,
                                                   @NonNull List<String> listDataHeader) {

        Map<String, List<String>> map = new HashMap<>();

        for (String header : listDataHeader) {

            map.put(header, Collections.singletonList("-"));
        }

        return new BasicExpandableListAdapter(context, listDataHeader, map);
    }

    @Override
    public int getGroupCount() {

        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {

        return listDataChild.get(listDataHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {

        return listDataHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {

        return listDataChild.get(listDataHeader.get(i)).get(i1);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {

        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(
            int groupPosition, int childPosition, boolean b, View convertView, ViewGroup viewGroup) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
