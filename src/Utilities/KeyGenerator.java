package Utilities;

import java.util.concurrent.ThreadLocalRandom;

public class KeyGenerator {

    int maxNum(String key) {
        int[] nums = new int[key.length()];
        int i;
        for (i = 0; i < key.length(); i++)
            nums[i] = Character.getNumericValue(key.charAt(i));
        int max = nums[0];
        i--;
        for (; i > 0; i--) {
            if (nums[i] > max)
                max = nums[i];
        }
        return max;
    }

    public String checkNums(String theKey) {
        String[] args = theKey.split(" ");
        StringBuilder[] keys = new StringBuilder[args.length];
        int i;
        for (i = 0; i < keys.length; i++) {
            keys[i] = new StringBuilder();
            keys[i].append(args[i]);
        }
        //if(i > 0)
        i--;
        int max, biggest, maxPos;
        for (; i >= 0; i--) {
            while ((max = maxNum(keys[i].toString())) > keys[i].length()) {
                biggest = keys[i].length();
                while (keys[i].toString().contains("" + biggest))
                    biggest--;
                maxPos = keys[i].indexOf("" + max);
                keys[i].replace(maxPos, maxPos + 1, "" + biggest);
            }
        }
        for (i = 1; i < keys.length; i++) {
            keys[0].append(" " + keys[i].toString());
        }
        return keys[0].toString();
    }

    public boolean keyCheck(String theKey) {
        int vozr, ubiv, i, err;
        String[] keys = theKey.split(" ");
        err = 0;
        for (int j = 0; j < keys.length; j++) {
            i = 1;
            vozr = ubiv = 0;
            while (i < keys[j].length()) {
                if (Character.getNumericValue(keys[j].charAt(i - 1)) > Character.getNumericValue(keys[j].charAt(i)))
                    ubiv++;
                else if (Character.getNumericValue(keys[j].charAt(i - 1)) < Character.getNumericValue(keys[j].charAt(i)))
                    vozr++;
                i++;
            }
            if (vozr == keys[j].length() - 1 || ubiv == keys[j].length() - 1)
                err++;
            //return false;
        }
        if (err == keys.length)
            return false;
        return true;
    }

    public String generate() {
        StringBuilder key = new StringBuilder();
        int keyCount = ThreadLocalRandom.current().nextInt(1, 11);
        int numCount;
        int temp;
        StringBuilder singleKey = new StringBuilder();
        for (int i = 0; i < keyCount; i++) {
            numCount = ThreadLocalRandom.current().nextInt(3, 10);
            for (int j = 0; j < numCount; j++) {
                temp = ThreadLocalRandom.current().nextInt(1, numCount + 1);
                while (singleKey.toString().contains(temp + "")) {
                    temp++;
                    if (temp > numCount)
                        temp = 1;
                }
                singleKey.append(temp);
            }
            key.append(singleKey.toString());
            singleKey.delete(0, singleKey.length());
            if (i != keyCount - 1)
                key.append(" ");
        }
        return key.toString();
    }
}
