package james.metronome.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import james.metronome.R;
import james.metronome.data.ThemeData;
import rx.Subscription;
import rx.functions.Action1;

public class ThemesView extends LinearLayout {

    public static final ThemeData[] themes = new ThemeData[]{
            new ThemeData(R.string.title_theme_light, R.color.colorPrimary, R.color.colorAccent, R.color.colorBackground),
            new ThemeData(R.string.title_theme_wood, R.color.colorPrimaryWood, R.color.colorAccentDark, R.color.colorPrimaryWood),
            new ThemeData(R.string.title_theme_dark, R.color.colorPrimaryDark, R.color.colorAccentDark, R.color.colorBackgroundDark),
            new ThemeData(R.string.title_theme_amoled, R.color.colorPrimaryAmoled, R.color.colorAccentDark, R.color.colorPrimaryAmoled)
    };

    private OnThemeChangedListener listener;

    private boolean isExpanded;
    private int theme;

    private Integer colorAccent;
    private Integer textColorPrimary;
    private Integer textColorPrimaryInverse;

    private Subscription colorAccentSubscription;
    private Subscription textColorPrimarySubscription;
    private Subscription textColorPrimaryInverseSubscription;

    public ThemesView(Context context) {
        this(context, null);
    }

    public ThemesView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < themes.length; i++) {
            View v = inflater.inflate(R.layout.item_theme, this, false);
            v.setTag(i);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = (int) v.getTag();
                    if (isExpanded) {
                        if (theme != position && listener != null) {
                            theme = position;
                            themes[theme].apply(getContext());
                            listener.onThemeChanged(position);
                        }

                        isExpanded = false;
                        for (int i = 0; i < getChildCount(); i++) {
                            final View view = getChildAt(i);

                            if (view.findViewById(R.id.background).getAlpha() == 1) {
                                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), textColorPrimaryInverse, textColorPrimary);
                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        view.findViewById(R.id.background).setAlpha(1 - animation.getAnimatedFraction());
                                        ((TextView) view.findViewById(R.id.name)).setTextColor((int) animation.getAnimatedValue());
                                    }
                                });
                                animator.start();

                                ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), textColorPrimaryInverse, colorAccent);
                                animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        ((ImageView) view.findViewById(R.id.image)).setColorFilter((int) animation.getAnimatedValue());
                                    }
                                });
                                animator2.start();
                            }

                            if (theme != i)
                                view.setVisibility(View.GONE);
                        }
                    } else {
                        isExpanded = true;
                        for (int i = 0; i < getChildCount(); i++) {
                            final View view = getChildAt(i);
                            view.setVisibility(View.VISIBLE);
                            if (theme == i) {
                                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), textColorPrimary, textColorPrimaryInverse);
                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        view.findViewById(R.id.background).setAlpha(animation.getAnimatedFraction());
                                        ((TextView) view.findViewById(R.id.name)).setTextColor((int) animation.getAnimatedValue());
                                    }
                                });
                                animator.start();

                                ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), colorAccent, textColorPrimaryInverse);
                                animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        ((ImageView) view.findViewById(R.id.image)).setColorFilter((int) animation.getAnimatedValue());
                                    }
                                });
                                animator2.start();
                            }
                        }
                    }
                }
            });

            ((TextView) v.findViewById(R.id.name)).setText(themes[i].getName(getContext()));
            if (i != theme)
                v.setVisibility(View.GONE);

            addView(v);
        }
    }

    public void subscribe() {
        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        colorAccent = integer;
                        for (int i = 0; i < getChildCount(); i++) {
                            View v = getChildAt(i);
                            v.findViewById(R.id.background).setBackgroundColor(integer);
                            if (!isExpanded || theme != i)
                                ((ImageView) v.findViewById(R.id.image)).setColorFilter(integer);
                        }
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        textColorPrimary = integer;
                        for (int i = 0; i < getChildCount(); i++) {
                            View v = getChildAt(i);
                            if (!isExpanded || theme != i)
                                ((TextView) v.findViewById(R.id.name)).setTextColor(integer);
                        }
                    }
                });

        textColorPrimaryInverseSubscription = Aesthetic.get()
                .textColorPrimaryInverse()
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        textColorPrimaryInverse = integer;
                        for (int i = 0; i < getChildCount(); i++) {
                            View v = getChildAt(i);
                            if (isExpanded && theme == i) {
                                ((TextView) v.findViewById(R.id.name)).setTextColor(integer);
                                ((ImageView) v.findViewById(R.id.image)).setColorFilter(integer);
                            }
                        }
                    }
                });
    }

    public void unsubscribe() {
        colorAccentSubscription.unsubscribe();
        textColorPrimarySubscription.unsubscribe();
        textColorPrimaryInverseSubscription.unsubscribe();
    }

    public void setListener(OnThemeChangedListener listener) {
        this.listener = listener;
    }

    public void setTheme(int theme) {
        this.theme = theme;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(i == theme ? View.VISIBLE : View.GONE);
        }
    }

    public interface OnThemeChangedListener {
        void onThemeChanged(int theme);
    }

}