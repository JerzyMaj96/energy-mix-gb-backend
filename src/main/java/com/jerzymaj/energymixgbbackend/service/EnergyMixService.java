package com.jerzymaj.energymixgbbackend.service;

import com.jerzymaj.energymixgbbackend.DTOs.DailyEnergySummary;
import com.jerzymaj.energymixgbbackend.DTOs.EnergyMixInterval;
import com.jerzymaj.energymixgbbackend.DTOs.EnergyResponse;
import com.jerzymaj.energymixgbbackend.DTOs.Fuel;
import com.jerzymaj.energymixgbbackend.exceptions.NoEnergyMixIntervalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnergyMixService {

    private final RestClient restClient;

    private static final List<String> CLEAN_ENERGY = List.of("biomass", "nuclear", "hydro", "wind", "solar");

    public EnergyMixService(@Value("${api.carbon-intensity.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Retrieves energy generation data from the external API for a specific date range.
     *
     * @param from Start date
     * @param to   End date
     * @return {@link EnergyResponse} containing a list of energy intervals.
     */

    public EnergyResponse getEnergyData(String from, String to) {

        return restClient.get()
                .uri("/generation/{from}/{to}", from, to)
                .retrieve()
                .body(EnergyResponse.class);
    }

    /**
     * Aggregates a list of 30-minute intervals into a single daily summary.
     * Calculates the average renewable energy percentage and the average mix for each fuel type.
     *
     * @param intervalsList list of intervals for a single day.
     * @param date          date string representing the day.
     * @return {@link DailyEnergySummary} object containing average values for the day.
     * @throws NoEnergyMixIntervalException if the provided list is empty.
     */

    private DailyEnergySummary calculateDailyEnergySummary(List<EnergyMixInterval> intervalsList, String date) {
        if (intervalsList.isEmpty()) {
            throw new NoEnergyMixIntervalException("No intervals found for given date");
        }

        double cleanEnergyPercentSum = 0;
        Map<String, Double> fuelPercentageSum = new HashMap<>();

        for (EnergyMixInterval interval : intervalsList) {
            cleanEnergyPercentSum += calculateCleanEnergyPercent(interval);

            for (Fuel fuel : interval.generationMix()) {
                fuelPercentageSum.merge(fuel.fuel(), fuel.percentage(), Double::sum);
            }
        }

        int intervalNum = intervalsList.size();

        double averageRenewablePercent = cleanEnergyPercentSum / intervalNum;

        Map<String, Double> fuelPercentAverages = new HashMap<>();

        for (Map.Entry<String, Double> entry : fuelPercentageSum.entrySet()) {
            fuelPercentAverages.put(entry.getKey(), entry.getValue() / intervalNum);
        }

        return new DailyEnergySummary(date, averageRenewablePercent, fuelPercentAverages);
    }

    /**
     * Helper method that calculates the total percentage of renewable energy
     * for a single interval.
     *
     * @param interval single energy mix interval.
     * @return sum of percentages for all renewable fuel types.
     */

    private double calculateCleanEnergyPercent(EnergyMixInterval interval) {

        double percentageSum = 0;

        for (Fuel fuel : interval.generationMix()) {
            if (CLEAN_ENERGY.contains(fuel.fuel())) {
                percentageSum += fuel.percentage();
            }
        }

        return percentageSum;
    }

    /**
     *
     */

    public List<DailyEnergySummary> calculateThreeDaysSummary() {

        String today = LocalDate.now().toString();
        String inThreeDays = LocalDate.now().plusDays(3).toString();

        EnergyResponse energyResponse = getEnergyData(today, inThreeDays);

        Map<String, List<EnergyMixInterval>> groupedByDayIntervals = new HashMap<>();

        for (EnergyMixInterval interval : energyResponse.data()) {
            String dateKey = interval.from().substring(0,10);

            if(!groupedByDayIntervals.containsKey(dateKey)) {
                groupedByDayIntervals.put(dateKey, new ArrayList<>());
            }

            groupedByDayIntervals.get(dateKey).add(interval);
        }

        List<DailyEnergySummary>  dailyEnergySummaryList = new ArrayList<>();

        for (String dateKey : groupedByDayIntervals.keySet()) {
            List<EnergyMixInterval> intervalList = groupedByDayIntervals.get(dateKey);

            DailyEnergySummary dailyEnergySummary = calculateDailyEnergySummary(intervalList, dateKey);

            dailyEnergySummaryList.add(dailyEnergySummary);
        }

        dailyEnergySummaryList.sort((summary1, summary2) -> summary1.date().compareTo(summary2.date()));

        return dailyEnergySummaryList;
    }
}
