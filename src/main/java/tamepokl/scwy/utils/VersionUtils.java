package tamepokl.scwy.utils;

public class VersionUtils {
    public static int compareVersion(String v1, String v2) {
        if (v1 == null || v2 == null) {
            throw new IllegalArgumentException("Version cant be null");
        }

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return -1;
            }
        }

        return 0;
    }
}
