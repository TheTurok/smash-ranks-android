package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.garpr.android.R;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.Console;
import com.garpr.android.models.LogMessage;
import com.garpr.android.models.LogMessage.Level;


public class ConsoleActivity extends BaseListActivity implements
        Console.Listener {


    private static final String TAG = "ConsoleActivity";

    private MenuItem mClearLog;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, ConsoleActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_console;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLoading(false);
        setAdapter(new ConsoleAdapter());
    }


    @Override
    public void onLogMessagesChanged() {
        notifyDataSetChanged();

        if (Console.hasLogMessages()) {
            mClearLog.setEnabled(true);
        } else {
            mClearLog.setEnabled(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_console_menu_clear_log:
                mClearLog.setEnabled(false);
                Console.clearLogMessages();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Console.detachListener(this);
    }


    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        mClearLog = menu.findItem(R.id.activity_console_menu_clear_log);

        if (Console.hasLogMessages()) {
            mClearLog.setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onRefresh() {
        super.onRefresh();
        Toast.makeText(this, R.string.no_gorgonite_johns, Toast.LENGTH_SHORT).show();
        setLoading(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Console.attachListener(this);
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.setVerticalScrollBarEnabled(false);
    }


    @Override
    public String toString() {
        return TAG;
    }


    @Override
    protected boolean showDrawerIndicator() {
        return false;
    }




    private final class ConsoleAdapter extends BaseListAdapter<RecyclerView.ViewHolder> {


        private final int mDebugTextColor;
        private final int mErrorTextColor;
        private final int mWarningTextColor;


        private ConsoleAdapter() {
            super(ConsoleActivity.this, getRecyclerView());

            final Resources res = getResources();
            mDebugTextColor = res.getColor(R.color.white);
            mErrorTextColor = res.getColor(R.color.console_error);
            mWarningTextColor = res.getColor(R.color.console_warning);
        }


        private void formatTextViewPerLevel(final LogMessage logMessage, final TextView textView) {
            final Level level = logMessage.getLevel();

            switch (level) {
                case DEBUG:
                    textView.setTextColor(mDebugTextColor);
                    break;

                case ERROR:
                    textView.setTextColor(mErrorTextColor);
                    break;

                case WARNING:
                    textView.setTextColor(mWarningTextColor);
                    break;

                default:
                    throw new RuntimeException("Unknown Level: " + level);
            }
        }


        @Override
        public int getItemCount() {
            return Console.getLogMessagesSize();
        }


        @Override
        public long getItemId(final int position) {
            return Console.getLogMessage(position).getId();
        }


        @Override
        public int getItemViewType(final int position) {
            final LogMessage logMessage = Console.getLogMessage(position);
            final int itemViewType;

            if (logMessage.hasStackTrace()) {
                itemViewType = LogMessageWithStackTraceViewHolder.VIEW_TYPE;
            } else {
                itemViewType = LogMessageViewHolder.VIEW_TYPE;
            }

            return itemViewType;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final int itemViewType = getItemViewType(position);
            final LogMessage logMessage = Console.getLogMessage(position);

            switch (itemViewType) {
                case LogMessageViewHolder.VIEW_TYPE:
                    final LogMessageViewHolder lmvh = (LogMessageViewHolder) holder;
                    lmvh.mTagAndMessage.setText(logMessage.getMessage());
                    formatTextViewPerLevel(logMessage, lmvh.mTagAndMessage);
                    break;

                case LogMessageWithStackTraceViewHolder.VIEW_TYPE:
                    final LogMessageWithStackTraceViewHolder lmwstvh = (LogMessageWithStackTraceViewHolder) holder;
                    lmwstvh.mTagAndMessage.setText(logMessage.getMessage());
                    lmwstvh.mStackTrace.setText(logMessage.getStackTrace());
                    formatTextViewPerLevel(logMessage, lmwstvh.mTagAndMessage);
                    formatTextViewPerLevel(logMessage, lmwstvh.mStackTrace);
                    break;

                default:
                    throw new RuntimeException("Unknown view type: " + itemViewType);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                final int viewType) {
            final LayoutInflater inflater = getLayoutInflater();

            final View view;
            final RecyclerView.ViewHolder holder;

            switch (viewType) {
                case LogMessageViewHolder.VIEW_TYPE:
                    view = inflater.inflate(R.layout.model_log_message, parent, false);
                    holder = new LogMessageViewHolder(view);
                    break;

                case LogMessageWithStackTraceViewHolder.VIEW_TYPE:
                    view = inflater.inflate(R.layout.model_log_message_with_stack_trace, parent, false);
                    holder = new LogMessageWithStackTraceViewHolder(view);
                    break;

                default:
                    throw new RuntimeException("Illegal viewType detected: " + viewType);
            }

            return holder;
        }


    }


    private static final class LogMessageViewHolder extends RecyclerView.ViewHolder {


        private static final int VIEW_TYPE = 1;
        private final TextView mTagAndMessage;


        private LogMessageViewHolder(final View view) {
            super(view);
            mTagAndMessage = (TextView) view.findViewById(R.id.model_log_message_tag_and_message);
        }


    }


    private static final class LogMessageWithStackTraceViewHolder extends RecyclerView.ViewHolder {


        private static final int VIEW_TYPE = 2;
        private final TextView mStackTrace;
        private final TextView mTagAndMessage;


        private LogMessageWithStackTraceViewHolder(final View view) {
            super(view);
            mStackTrace = (TextView) view.findViewById(R.id.model_log_message_with_stack_trace_stack_trace);
            mTagAndMessage = (TextView) view.findViewById(R.id.model_log_message_with_stack_trace_tag_and_message);
        }


    }


}
