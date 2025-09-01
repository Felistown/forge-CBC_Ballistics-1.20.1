package net.felis.cbc_ballistics.util;

public class StringToInt {

    public static boolean posFromString(String string, int[] array) {
            for(int i = 0; i <= 2; i ++) {
                boolean pass = false;
                boolean found = false;
                int index = 0;
                for (int j = 0; j < string.length(); j++) {
                    try {
                        Integer.valueOf(string.substring(j, j + 1));
                        if(!found) {
                            index = j;
                            found = true;
                        }
                    } catch (NumberFormatException e) {
                        if (found) {
                            try {
                                array[i] = Integer.parseInt(string.substring(index, j));
                            } catch (NumberFormatException e1) {
                                return false;
                            }
                            string = string.substring(j);
                            pass = true;
                            break;
                        } else {
                            if (string.charAt(j) == '-') {
                                index = j;
                                found = true;
                            }
                        }
                    }
                }
                if(!pass) {
                    if(found) {
                        try {
                            array[i] = Integer.parseInt(string.substring(index));
                            string = "";
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return string.isEmpty();
    }
}
