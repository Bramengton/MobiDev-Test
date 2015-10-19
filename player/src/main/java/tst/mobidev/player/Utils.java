package tst.mobidev.player;

/**
 * @author by Bramengton
 * @date 07.10.15.
 */
public class Utils {

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";


        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);

        if(hours > 0){
            finalTimerString = hours + ":";
        }

        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }

    public int getPercent(int currentDuration, int totalDuration){
        return (int)(((float)currentDuration/totalDuration)*100);
    }

    public int getProgress(int progress, int totalDuration){
        return  (totalDuration / 100) * progress;
    }
}
