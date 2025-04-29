package com.data_management;

import com.cardio_generator.HealthDataSimulator;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("HealthDataStorage")) {
            //com.cardio_generator.HealthDataSimulator.main(new String[]{});
            com.data_management.DataStorage.main(new String[]{});
        } else {
            //com.data_management.DataStorage.main(new String[]{});
            com.cardio_generator.HealthDataSimulator.main(new String[]{});
        }
    }
}
