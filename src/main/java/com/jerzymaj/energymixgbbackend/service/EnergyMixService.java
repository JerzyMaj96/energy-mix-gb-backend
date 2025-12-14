package com.jerzymaj.energymixgbbackend.service;

import com.jerzymaj.energymixgbbackend.DTOs.*;
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

    public EnergyMixService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Retrieves energy generation data from the external API for a specific date range.
     *
     * @param from start Datetime in ISO8601 format
     * @param to   end Datetime in ISO8601 format
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
        Map<String, Double> fuelPercentSum = new HashMap<>();

        for (EnergyMixInterval interval : intervalsList) {
            cleanEnergyPercentSum += calculateCleanEnergyPercent(interval);

            for (Fuel fuel : interval.generationMix()) {
                fuelPercentSum.merge(fuel.fuel(), fuel.percentage(), Double::sum);
            }
        }

        int intervalNum = intervalsList.size();

        double averageCleanEnergyPercent = cleanEnergyPercentSum / intervalNum;

        Map<String, Double> fuelPercentAverages = new HashMap<>();

        for (Map.Entry<String, Double> entry : fuelPercentSum.entrySet()) {
            fuelPercentAverages.put(entry.getKey(), entry.getValue() / intervalNum);
        }

        return new DailyEnergySummary(date, averageCleanEnergyPercent, fuelPercentAverages);
    }

    /**
     * Helper method that calculates the total percentage of renewable energy
     * for a single interval.
     *
     * @param interval single energy mix interval.
     * @return sum of percentages for all renewable fuel types.
     */

    private double calculateCleanEnergyPercent(EnergyMixInterval interval) {

        double percentSum = 0;

        for (Fuel fuel : interval.generationMix()) {
            if (CLEAN_ENERGY.contains(fuel.fuel())) {
                percentSum += fuel.percentage();
            }
        }

        return percentSum;
    }

    /**
     * Fetches energy data for today and the next 3 days.
     * It groups the data manually by date and calculates the daily averages.
     * Finally, it sorts the result by date.
     *
     * @return list of {@link DailyEnergySummary} objects sorted by date
     */

    public List<DailyEnergySummary> calculateThreeDaysSummary() {

        String today = LocalDate.now().toString();
        String inThreeDays = LocalDate.now().plusDays(2).toString();

        EnergyResponse energyResponse = getEnergyData(today, inThreeDays);

        if (energyResponse.data() == null || energyResponse.data().isEmpty()) {
            throw new NoEnergyMixIntervalException("Returned list is empty");
        }

        Map<String, List<EnergyMixInterval>> groupedByDayIntervals = new HashMap<>();


        for (EnergyMixInterval interval : energyResponse.data()) {
            String dateKey = interval.from().substring(0, 10);

            if (!groupedByDayIntervals.containsKey(dateKey)) {
                groupedByDayIntervals.put(dateKey, new ArrayList<>());
            }

            groupedByDayIntervals.get(dateKey).add(interval);
        }

        List<DailyEnergySummary> dailyEnergySummaryList = new ArrayList<>();


        for (String dateKey : groupedByDayIntervals.keySet()) {
            List<EnergyMixInterval> intervalList = groupedByDayIntervals.get(dateKey);

            DailyEnergySummary dailyEnergySummary = calculateDailyEnergySummary(intervalList, dateKey);

            dailyEnergySummaryList.add(dailyEnergySummary);
        }

        dailyEnergySummaryList.sort((summary1, summary2) -> summary1.date().compareTo(summary2.date()));

        return dailyEnergySummaryList;
    }

    /**
     * Finds the best time window for charging electric vehicle.
     * It analyzes forecasts for tomorrow and the day after tomorrow.
     *
     * @param windowLength duration of charging in hours
     * @return {@link OptimalChargingWindow} with the highest average clean energy.
     */

    public OptimalChargingWindow calculateOptimalChargingWindow(int windowLength) {

        String tomorrowStart = LocalDate.now().plusDays(1).toString();
        String dayAfterTomorrowEnd = LocalDate.now().plusDays(3).toString();

        EnergyResponse energyResponse = getEnergyData(tomorrowStart, dayAfterTomorrowEnd);

        if (energyResponse.data() == null || energyResponse.data().isEmpty()) {
            throw new NoEnergyMixIntervalException("No intervals returned from API");
        }

        List<EnergyMixInterval> allIntervals = energyResponse.data();

        int windowSize = windowLength * 2;

        double maxAverage = Double.NEGATIVE_INFINITY;
        EnergyMixInterval bestStartInterval = null;

        for (int i = 0; i <= allIntervals.size() - windowSize; i++) {

            List<EnergyMixInterval> currWindow = allIntervals.subList(i, i + windowSize);

            double currentAverage = calculateWindowAverage(currWindow);

            if (currentAverage > maxAverage) {
                maxAverage = currentAverage;
                bestStartInterval = allIntervals.get(i);
            }
        }

        if (bestStartInterval == null) {
            throw new NoEnergyMixIntervalException("No intervals found for given date");
        }

        int startIndex = allIntervals.indexOf(bestStartInterval);
        int endIndex = startIndex + windowSize - 1;
        EnergyMixInterval bestEndInterval = allIntervals.get(endIndex);

        return new OptimalChargingWindow(bestStartInterval.from(), bestEndInterval.to(), maxAverage);
    }

    private double calculateWindowAverage(List<EnergyMixInterval> window) {
        double sum = 0;

        for (EnergyMixInterval interval : window) {
            sum += calculateCleanEnergyPercent(interval);
        }

        return sum / window.size();
    }
}
