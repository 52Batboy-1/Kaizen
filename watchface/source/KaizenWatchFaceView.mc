import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.System;
import Toybox.ActivityMonitor;
import Toybox.Time;
import Toybox.Time.Gregorian;
import Toybox.Lang;
import Toybox.Application;

// FR955 Solar — 416 x 416 round display
class KaizenWatchFaceView extends WatchUi.WatchFace {

    private const CX as Lang.Number = 208;

    // Palette matches the Android app
    private const C_BG     as Lang.Number = 0x000000;
    private const C_GOLD   as Lang.Number = 0xFFB800;
    private const C_MUTED  as Lang.Number = 0x6B7280;
    private const C_TRACK  as Lang.Number = 0x1F2937;
    private const C_RED    as Lang.Number = 0xFF6B6B;
    private const C_TEAL   as Lang.Number = 0x4ECDC4;
    private const C_YELLOW as Lang.Number = 0xFFE66D;
    private const C_PURPLE as Lang.Number = 0xA78BFA;
    private const C_GREEN  as Lang.Number = 0x4ADE80;

    function initialize() {
        WatchFace.initialize();
    }

    function onLayout(dc as Graphics.Dc) as Void {
    }

    function onShow() as Void {
    }

    function onHide() as Void {
    }

    function onExitSleep() as Void {
        WatchUi.requestUpdate();
    }

    function onEnterSleep() as Void {
        WatchUi.requestUpdate();
    }

    function onUpdate(dc as Graphics.Dc) as Void {
        dc.setColor(C_BG, C_BG);
        dc.clear();

        var now = Gregorian.info(Time.now(), Time.FORMAT_SHORT);
        drawDate(dc, now);
        drawTime(dc, now);
        drawWorkout(dc, now);
        drawMetrics(dc);
        drawBattery(dc);
        drawBranding(dc);
    }

    // ── Sections ────────────────────────────────────────────────────────────

    private function drawDate(dc as Graphics.Dc, now as Gregorian.Info) as Void {
        var days   = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];
        var months = ["", "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                      "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
        var s = days[now.day_of_week - 1] + "  " + months[now.month] + " " + now.day.toString();
        dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 55, Graphics.FONT_TINY, s, Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawTime(dc as Graphics.Dc, now as Gregorian.Info) as Void {
        var timeStr = now.hour.format("%02d") + ":" + now.min.format("%02d");
        dc.setColor(C_GOLD, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 108, Graphics.FONT_NUMBER_HOT, timeStr, Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawWorkout(dc as Graphics.Dc, now as Gregorian.Info) as Void {
        var week    = getStoredWeek();
        var workout = workoutForDay(now.day_of_week, week);

        if (workout == null) {
            dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
            dc.drawText(CX, 224, Graphics.FONT_SMALL, "REST", Graphics.TEXT_JUSTIFY_CENTER);
            return;
        }

        var label = workout[0] as Lang.String;
        var color = workout[1] as Lang.Number;

        // Colored pill
        dc.setColor(color, Graphics.COLOR_TRANSPARENT);
        dc.fillRoundedRectangle(CX - 72, 218, 144, 30, 15);

        dc.setColor(C_BG, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 220, Graphics.FONT_SMALL, label, Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawMetrics(dc as Graphics.Dc) as Void {
        // Vertical divider between HR and Steps
        dc.setColor(C_TRACK, Graphics.COLOR_TRANSPARENT);
        dc.fillRectangle(CX - 1, 264, 2, 32);

        // ── Heart rate ──────────────────────────────────
        var hr      = readHeartRate();
        var hrStr   = (hr > 0) ? (hr.toString() + " bpm") : "--";
        var hrColor = (hr > 0) ? C_RED : C_MUTED;

        dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX - 66, 264, Graphics.FONT_TINY, "HEART RATE", Graphics.TEXT_JUSTIFY_CENTER);
        dc.setColor(hrColor, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX - 66, 279, Graphics.FONT_SMALL, hrStr, Graphics.TEXT_JUSTIFY_CENTER);

        // ── Steps ───────────────────────────────────────
        var info     = ActivityMonitor.getInfo();
        var steps    = (info != null && info.steps    != null) ? info.steps    : 0;
        var stepGoal = (info != null && info.stepGoal != null) ? info.stepGoal : 10000;
        var pct      = (stepGoal > 0) ? (steps * 100 / stepGoal) : 0;

        var stepsColor = (pct >= 100) ? C_GREEN : (pct >= 50) ? C_YELLOW : C_MUTED;

        dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX + 66, 264, Graphics.FONT_TINY, "STEPS", Graphics.TEXT_JUSTIFY_CENTER);
        dc.setColor(stepsColor, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX + 66, 279, Graphics.FONT_SMALL, formatSteps(steps), Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawBattery(dc as Graphics.Dc) as Void {
        var stats = System.getSystemStats();
        var pct   = stats.battery.toNumber();
        if (pct < 0)   { pct = 0;   }
        if (pct > 100) { pct = 100; }

        var barW = 130;
        var barH = 7;
        var barX = CX - barW / 2;
        var barY = 330;

        // Track
        dc.setColor(C_TRACK, Graphics.COLOR_TRANSPARENT);
        dc.fillRoundedRectangle(barX, barY, barW, barH, 3);

        // Fill
        var fillW = barW * pct / 100;
        if (fillW < 3)    { fillW = 3;    }
        if (fillW > barW) { fillW = barW; }

        var batColor = (pct > 50) ? C_GREEN : (pct > 20) ? C_YELLOW : C_RED;
        dc.setColor(batColor, Graphics.COLOR_TRANSPARENT);
        dc.fillRoundedRectangle(barX, barY, fillW, barH, 3);

        // Label
        dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 342, Graphics.FONT_TINY, pct.toString() + "%", Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawBranding(dc as Graphics.Dc) as Void {
        // "K" in gold, "aizen" in muted — right-justify "K" at CX so they touch
        dc.setColor(C_GOLD, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 374, Graphics.FONT_TINY, "K", Graphics.TEXT_JUSTIFY_RIGHT);
        dc.setColor(C_MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(CX, 374, Graphics.FONT_TINY, "aizen", Graphics.TEXT_JUSTIFY_LEFT);
    }

    // ── Workout schedule ─────────────────────────────────────────────────────
    // Matches KaizenViewModel.scheduledWorkoutForDate logic exactly.
    // dow: 1=Sun 2=Mon 3=Tue 4=Wed 5=Thu 6=Fri 7=Sat

    private function workoutForDay(dow as Lang.Number, week as Lang.Number) as Lang.Array? {
        if (week <= 6) {
            // Foundation: Full Body Mon / Wed / Fri
            if (dow == 2 || dow == 4 || dow == 6) {
                return ["FULL BODY", C_PURPLE] as Lang.Array;
            }
        } else if (week <= 12) {
            // Development: Push Mon+Thu, Legs Tue+Fri
            if (dow == 2 || dow == 5) { return ["PUSH", C_RED]    as Lang.Array; }
            if (dow == 3 || dow == 6) { return ["LEGS", C_YELLOW] as Lang.Array; }
        } else if (week <= 18) {
            // Strength: PPL + 2 rest days cycle
            var idx = daysSinceEpoch() % 5;
            if (idx == 0) { return ["PUSH", C_RED]    as Lang.Array; }
            if (idx == 1) { return ["PULL", C_TEAL]   as Lang.Array; }
            if (idx == 2) { return ["LEGS", C_YELLOW] as Lang.Array; }
        } else {
            // Mastery: PPL + Full Body + 2 rest days cycle
            var idx = daysSinceEpoch() % 6;
            if (idx == 0) { return ["PUSH",      C_RED]    as Lang.Array; }
            if (idx == 1) { return ["PULL",      C_TEAL]   as Lang.Array; }
            if (idx == 2) { return ["LEGS",      C_YELLOW] as Lang.Array; }
            if (idx == 3) { return ["FULL BODY", C_PURPLE] as Lang.Array; }
        }
        return null;
    }

    // Days since 2024-01-01 — same epoch the Android app uses
    private function daysSinceEpoch() as Lang.Number {
        var epoch = new Time.Moment(1704067200l);
        var diff  = Time.now().subtract(epoch);
        return (diff.value() / 86400l).toNumber();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private function getStoredWeek() as Lang.Number {
        try {
            var val = Application.Properties.getValue("currentWeek");
            if (val instanceof Lang.Number) {
                var w = val as Lang.Number;
                if (w >= 1 && w <= 24) { return w; }
            }
        } catch (e) { }
        return 1;
    }

    private function readHeartRate() as Lang.Number {
        try {
            var iter = ActivityMonitor.getHeartRateHistory(1, true);
            if (iter != null) {
                var sample = iter.next();
                if (sample != null &&
                    sample.heartRate != ActivityMonitor.INVALID_HR_SAMPLE) {
                    return sample.heartRate;
                }
            }
        } catch (e) { }
        return 0;
    }

    private function formatSteps(steps as Lang.Number) as Lang.String {
        if (steps >= 10000) {
            return (steps / 1000).toString() + "k";
        }
        if (steps >= 1000) {
            var thousands = steps / 1000;
            var hundreds  = (steps % 1000) / 100;
            return thousands.toString() + "." + hundreds.toString() + "k";
        }
        return steps.toString();
    }
}
