package ga.lupuss.planlekcji.ui.builders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.Pair;
import android.support.v7.widget.PopupMenu;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;


final class LessonLayoutBuilder {

    private ConstraintLayout mainLayout;
    private LayoutInflater inflater;
    @NonNull
    static ConstraintLayout empty(
            @NonNull LayoutInflater inflater, ViewGroup parent) {

        return new LessonLayoutBuilder(inflater, parent, true).build();
    }

    static LessonLayoutBuilder filled(
            @NonNull LayoutInflater inflater, ViewGroup parent) {

        return new LessonLayoutBuilder(inflater, parent, false);
    }

    private  LessonLayoutBuilder(@NonNull LayoutInflater inflater,
                                 @NonNull ViewGroup parent,
                                 boolean isEmpty){

        this.inflater = inflater;

        if (!isEmpty) {

            this.mainLayout =
                    (ConstraintLayout) inflater.inflate(R.layout.lesson_div, parent, false);

        } else {

            this.mainLayout =
                    (ConstraintLayout) inflater.inflate(R.layout.empty_day_text, parent, false);
        }
    }

    @NonNull
    final LessonLayoutBuilder number(int i) {

        TextView textView = mainLayout.findViewById(R.id.lesson_number);
        textView.setText(String.valueOf(i));
        return this;
    }

    @NonNull
    final LessonLayoutBuilder hour(@NonNull Pair<String, String> pair) {

        LinearLayout layout = (LinearLayout)mainLayout.getChildAt(1);
        ((TextView)layout.findViewById(R.id.hours)).setText(pair.first + "\n" + pair.second);
        return this;
    }

    @NonNull
    final LessonLayoutBuilder lessons(@NonNull String... strings) {

        LinearLayout layout = mainLayout.findViewById(R.id.lesson_lines_linear);

        for (String str : strings) {

            TextView text = (TextView)inflater.inflate(R.layout.lesson_line, layout, false);
            text.setText(str);
            layout.addView(text);
        }
        return this;
    }

    @NonNull
    final LessonLayoutBuilder addPopUpMenu(OnTimetableItemClick onTimetableItemClick,
                                           @Nullable List<Pair<TimetableType, String>> links) {

        if (onTimetableItemClick == null || links == null) {

            mainLayout.setOnClickListener(view -> {}); // provide animation if menu items not allowed

        } else {

            mainLayout.setOnClickListener(view -> {

                if (links.size() == 0) {

                    return;
                }

                PopupMenu menu = new PopupMenu(
                        new ContextThemeWrapper(inflater.getContext(), R.style.PopupMenu), view);

                menu.setGravity(Gravity.RIGHT);


                for (int i = 0; i < links.size(); i++) {

                    menu.getMenu().add(Menu.NONE, i, i, links.get(i).second);
                }


                menu.setOnMenuItemClickListener(item -> {

                    int id = item.getItemId();

                    String slug = links.get(id).second;
                    TimetableType type = links.get(id).first;

                    onTimetableItemClick.onClick(type, slug);

                    return true;
                });

                menu.show();

            });
        }

        return this;
    }

    @NonNull
    final ConstraintLayout build() {

        return mainLayout;
    }
}
