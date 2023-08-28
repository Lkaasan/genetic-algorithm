import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;
import java.util.Iterator;

public class Modelling {

    // Initialise population size
    int populationSize = 100;

    // Arraylists
    ArrayList<String[]> priceData = new ArrayList<String[]>();
    ArrayList<int[]> inputData = new ArrayList<int[]>();

    public Modelling() {
        populatePriceData();
        createInputData();
    }

    // returns the data from the model
    public ArrayList<int[]> getInputData() {
        return this.inputData;
    }

    // function that creates the input data for the GA
    private void createInputData() {
        int numDays = 50;
        int secondNumDays = 14;
        int size = priceData.size();
        for (int i = (2 * numDays); i <= size - 14; i++) {
            int[] data = new int[7];
            if (smaCalculation(secondNumDays, i) > smaCalculation(numDays, i)) {
                data[0] = 1;
            } else {
                data[0] = 0;
            }
            if (emaCalculation(secondNumDays, i) > emaCalculation(numDays, i)) {
                data[1] = 1;
            } else {
                data[1] = 0;
            }
            if (emaCalculation(numDays, i) < getPrice(i)) {
                data[2] = 1;
            } else {
                data[2] = 0;
            }
            if (tbrCalculation(numDays, i) <= 0.00) {
                data[3] = 1;
            } else {
                data[3] = 0;
            }
            if (volCalculation(numDays, i) <= 0.03) {
                data[4] = 1;
            } else {
                data[4] = 0;
            }
            if (momCalculation(numDays, i) > 0) {
                data[5] = 1;
            } else {
                data[5] = 0;
            }
            data[6] = getIncrease(i);
            inputData.add(data);
        }
    }

    /**
     * function that returns the price for a given day
     * 
     * @param day of price
     * @return price
     */
    private double getPrice(int day) {
        int d = day - 1;
        String[] price = priceData.get(d);
        return Double.parseDouble(price[0]);
    }

    /**
     * function that gets weather the price will increase/decrease in 14 days for a
     * given day
     * 
     * @param day
     * @return 0/1 for decrease/increase
     */
    private int getIncrease(int day) {
        int d = day - 1;
        String[] increase = priceData.get(d);
        return Integer.parseInt(increase[1].replaceAll(" ", ""));
    }

    /**
     * function that calculates the SMA value, given the number of days and the
     * start day
     * 
     * @param numDays  number of days
     * @param startDay the start day
     * @return smaCalculation
     */
    private double smaCalculation(int numDays, int startDay) {
        try {
            // System.out.println(startDay);
            double sum = 0.0;
            int day = startDay - 2;
            for (int i = 0; i < numDays; i++) {
                String[] line = priceData.get(day);
                sum += Double.parseDouble(line[0]);
                day--;
            }
            return Math.round((sum / numDays) * 100.0) / 100.0;
        } catch (IndexOutOfBoundsException e) {
            return 0.0;
        }
    }

    /**
     * function that calculates the EMA value, given the number of days and the
     * start day
     * 
     * @param numDays  number of days
     * @param startDay the start day
     * @return emaCalculation
     */
    private double emaCalculation(int numDays, int startDay) {
        int day = startDay - (numDays + 1);
        int counter = 1;
        double sum = 0.0;
        double multiplier = (double) 2 / (numDays + 1);
        while (counter <= numDays) {
            String[] line = priceData.get(day);
            double dayPrice = Double.parseDouble(line[0]);
            if (counter == 1) {
                sum = smaCalculation(numDays, day + 1);
            } else {
                sum = (dayPrice * multiplier) + (sum * (1 - multiplier));
            }
            day++;
            counter++;
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    /**
     * function that calculates the TBR value, given the number of days and the
     * start day
     * 
     * @param numDays  number of days
     * @param startDay the start day
     * @return tbrCalculation
     */
    private double tbrCalculation(int numDays, int startDay) {
        double max = 0.0;
        String[] startLine = priceData.get(startDay - 1);
        double startPrice = Double.parseDouble(startLine[0]);
        for (int i = startDay - 2; i > (startDay - 2) - numDays; i--) {
            String[] line = priceData.get(i);
            if (Double.parseDouble(line[0]) > max) {
                max = Double.parseDouble(line[0]);
            }
        }
        return Math.round(((startPrice - max) / max) * 100.0) / 100.0;
    }

    /**
     * function that calculates the VOL value, given the number of days and the
     * start day
     * 
     * @param numDays  number of days
     * @param startDay the start day
     * @return volCalculation
     */
    private double volCalculation(int numDays, int startDay) {
        double sum = 0.0;
        for (int i = startDay - 2; i > (startDay - 2) - numDays; i--) {
            String[] line = priceData.get(i);
            sum += Double.parseDouble(line[0]);
        }
        double mean = sum / numDays;
        double standardDeviation = 0.0;
        for (int i = (startDay - 2) - numDays; i < startDay - 1; i++) {
            String[] line = priceData.get(i);
            standardDeviation += (Double.parseDouble(line[0]) - mean) * (Double.parseDouble(line[0]) - mean);
        }
        standardDeviation = Math.sqrt(standardDeviation / numDays - 1);

        return Math.round((standardDeviation / smaCalculation(numDays, startDay)) * 100.0) / 100.0;
    }

    /**
     * function that calculates the MOM value, given the number of days and the
     * start day
     * 
     * @param numDays  number of days
     * @param startDay the start day
     * @return momCalculation
     */
    private double momCalculation(int numDays, int startDay) {
        String[] startDayLine = priceData.get(startDay - 1);
        String[] endDayLine = priceData.get(startDay - (1 + numDays));

        return Math.round((Double.parseDouble(startDayLine[0]) - Double.parseDouble(endDayLine[0])) * 100.0) / 100.0;
    }

    // function that reads the CSV file with the price data and populates the
    private void populatePriceData() {
        try {
            Scanner sc = new Scanner(new File("PriceData.csv"));
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(",");
                priceData.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    // Main that runs the modelling class and uses the output as a parameter for the
    // GA class
    public static void main(String[] args) {
        Modelling model = new Modelling();
        GA geneticAlgoritm = new GA(model.getInputData());
    }
}

class GA {

    // initialising the datasets for both training and testing data
    ArrayList<int[]> data = new ArrayList<int[]>();
    ArrayList<int[]> trainingData = new ArrayList<int[]>();
    ArrayList<int[]> testingData = new ArrayList<int[]>();

    // static final variables needed
    private static final int bits = 13;
    private static final int populationSize = 100;
    private static final int maxGenerations = 10;
    private static final double crossoverProbability = 0.8;
    private static final double mutationProbability = 0.2;
    private static final int tournamentSize = 7;
    private static final double training = 0.7;
    // random variable
    Random random = new Random();

    // 2D array to store the population
    int[][] population = new int[populationSize][bits];

    // array to store the fitness
    Double[] fitness = new Double[populationSize];

    // Constructor that runs the GA and prints the best rule
    public GA(ArrayList<int[]> inputData) {
        transformInputData(inputData);
        int trainingSize = (int) Math.round(data.size() * training);
        splitData(trainingSize - 1);
        generatePopulation();
        evaluate();
        for (int i = 0; i < maxGenerations; i++) {
            int[][] tempPopulation = new int[populationSize][bits];
            int currentIndividual = 0;
            while (currentIndividual < populationSize) {
                double randomProb = random.nextDouble();
                if (randomProb <= mutationProbability || (populationSize - currentIndividual) == 1) {
                    int parent = tournamentSelection();

                    int[] offspring = twoPointMutaiton(parent);

                    tempPopulation[currentIndividual] = offspring;
                    currentIndividual++;
                } else {
                    int p1 = tournamentSelection();
                    int p2 = tournamentSelection();

                    int[][] offspring = twoPointCrossover(p1, p2);

                    tempPopulation[currentIndividual] = offspring[0];
                    currentIndividual++;
                    tempPopulation[currentIndividual] = offspring[1];
                    currentIndividual++;
                }
            }
            population = tempPopulation;
            evaluate();
        }
        double f = 0.0;
        int index = 0;
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] > f) {
                f = fitness[i];
                index = i;
            }
        }
        System.out.println("Best Individual: " + index);
        System.out.println("Fitness: " + fitness[index]);
        System.out.print("[");
        for (int i = 0; i < bits; i++) {
            System.out.print(population[index][i]);
        }
        System.out.println("]");
        System.out.println(toString(population[index]));
        accuracy(population[index]);
    }

    /**
     * function that splits the inputted data from the modeelling class into
     * training and testing data
     * 
     * @param trainingSize, how many inputs are used for training
     */
    private void splitData(int trainingSize) {
        int counter = 0;
        Iterator<int[]> itr = data.iterator();
        while (itr.hasNext()) {
            if (counter <= trainingSize) {
                int[] line = itr.next();
                trainingData.add(line);
                counter++;
            } else {
                int[] line = itr.next();
                testingData.add(line);
            }
            itr.remove();
        }
    }

    /**
     * function that transforms the inputted data from the model into the right
     * representation for the individuals
     * 
     * @param inputtedData, and arraylist of all the inputted data
     */
    private void transformInputData(ArrayList<int[]> inputtedData) {
        for (int i = 0; i < inputtedData.size(); i++) {
            int[] line = inputtedData.get(i);
            int[] newLine = new int[inputtedData.size()];
            int counter = 0;
            for (int j = 0; j < 7; j++) {
                if (line[j] == 0 && j != 6) {
                    newLine[counter] = 0;
                    newLine[counter + 1] = 1;
                    counter += 2;
                } else if (line[j] == 1 && j != 6) {
                    newLine[counter] = 1;
                    newLine[counter + 1] = 0;
                    counter += 2;
                } else {
                    newLine[counter] = line[j];
                }
            }
            data.add(newLine);
        }
    }

    /**
     * function that performs tournament selection and returns the winner
     * 
     * @return individual selected
     */
    private int tournamentSelection() {
        int[] tournament = new int[tournamentSize];
        boolean[] selected = new boolean[populationSize];

        for (int i = 0; i < tournamentSize; i++) {
            int chosen = -1;
            while (chosen == -1) {
                chosen = random.nextInt(populationSize);
                if (!selected[chosen]) {
                    tournament[i] = chosen;
                    selected[chosen] = true;
                } else {
                    chosen = -1;
                }
            }
        }
        int winner = 0;
        for (int i = 1; i < tournamentSize; i++) {
            if (fitness[tournament[winner]] < fitness[tournament[i]]) {
                winner = i;
            }
        }
        return tournament[winner];
    }

    // function that calculates and stores the fitness of each individual in the
    // population
    private void evaluate() {
        for (int i = 0; i < populationSize; i++) {
            int TP = 0;
            int FP = 0;
            int FN = 0;
            int TN = 0;

            int target = population[i][bits - 1];

            for (int j = 0; j < trainingData.size(); j++) {
                int[] line = trainingData.get(j);
                boolean equal = false;
                if (target == line[bits - 1]) {
                    equal = true;
                }
                if (covers(population[i], line)) {
                    if (equal) {
                        TP++;
                    } else {
                        FP++;
                    }
                } else {
                    if (equal) {
                        FN++;
                    } else {
                        TN++;
                    }
                }
            }

            double sensitivity = TP / (TP + (double) FN);
            double specificity = TN / (FP + (double) TN);

            fitness[i] = sensitivity * specificity;

            if (Double.isNaN(fitness[i])) {
                fitness[i] = 0.0;
            }
        }
    }

    /**
     * function that checks if a rule covers an instance, returns true or false
     * 
     * @param rule,     an array of 0's and 1's for the rule
     * @param instance, an array of '0's and 1's for the instance
     * @return boolean, true if covers, false otherwise
     */
    private boolean covers(int[] rule, int[] instance) {
        int p = 0;
        boolean endReturn = true;
        for (int i = 0; i < 6; i++) {
            int length = 2;
            boolean match = false;
            int counter = 0;
            for (int j = 0; j < length; j++) {
                if (rule[p + j] == 1) {
                    counter++;
                    if (instance[p + j] == 1) {
                        match = true;
                    }
                }
            }
            if ((counter != length) && !match) {
                endReturn = false;
            }
            p += length;
        }
        return endReturn;
    }

    /**
     * function that performs a two point mutation, given a index for the parent
     * 
     * @param parent, index of the parent
     * @return offspring, an array representing the offspring
     */
    private int[] twoPointMutaiton(int parent) {
        int[] offspring = new int[bits];
        int point1 = random.nextInt(bits);
        int point2 = random.nextInt(bits);
        for (int i = 0; i < bits; i++) {
            if (i == point1 || i == point2) {
                if (population[parent][i] == 0) {
                    offspring[i] = 1;
                } else {
                    offspring[i] = 0;
                }
            } else {
                offspring[i] = population[parent][i];
            }
        }
        return offspring;
    }

    /**
     * function that performs a crossover, given two indexes for the parents
     * 
     * @param p1, index of first parent
     * @param p2, index of second parent
     * @return offspring, 2d Array representing the offsprings
     */
    private int[][] twoPointCrossover(int p1, int p2) {
        int[][] offspring = new int[2][bits];
        int startPoint = random.nextInt(bits / 2);
        int endPoint = random.nextInt(bits);
        while (endPoint <= startPoint) {
            endPoint = random.nextInt(bits);
        }
        boolean startFound = false;
        boolean endFound = false;
        for (int i = 0; i < bits; i++) {
            if (i == endPoint) {
                endFound = true;
            }
            if (i == startPoint || (startFound == true && endFound == false)) {
                startFound = true;
                offspring[0][i] = population[p2][i];
                offspring[1][i] = population[p1][i];
            } else {
                offspring[0][i] = population[p1][i];
                offspring[1][i] = population[p2][i];
            }
        }
        return offspring;
    }

    // function that generates the intiial population
    private void generatePopulation() {
        for (int i = 0; i < populationSize; i++) {
            population[i] = new int[bits];
            for (int j = 0; j < bits; j++) {
                population[i][j] = random.nextInt(2);
            }
        }
    }

    /**
     * function that calculates and displays how many instances the rule covers on
     * the test data, and the accuracy of that rule in terms of the prediction
     * 
     * @param rule, array of ints representing the rule
     */
    private void accuracy(int[] rule) {
        int instances = 0;
        int correctPredictions = 0;
        for (int i = 0; i < testingData.size(); i++) {
            int[] line = testingData.get(i);
            if (covers(rule, line)) {
                instances++;
                if (line[12] == rule[12]) {
                    correctPredictions++;
                }
            }
        }
        double acc = (double) correctPredictions / instances;
        System.out.println("Instances: " + instances);
        System.out.println("Accuracy: " + acc);
    }

    /**
     * function that converts the rule to a string
     * 
     * @param rule, array of ints representing the rule
     * @return rule as a string
     */
    private String toString(int[] rule) {
        String line = "IF ";
        for (int i = 0; i < 12; i += 2) {
            if (rule[i] != rule[i + 1]) {
                if (i == 0) {
                    if (rule[i] == 1) {
                        line += "{SMA(SHORT) > SMA(LONG) = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{SMA(SHORT) > SMA(LONG) = FALSE} ";
                    }
                } else if (i == 2) {
                    if (rule[i] == 1) {
                        line += "{EMA(SHORT) > EMA(LONG) = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{EMA(SHORT) > EMA(LONG) = FALSE} ";
                    }
                } else if (i == 4) {
                    if (rule[i] == 1) {
                        line += "{EMA < Price = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{EMA < Price = FALSE} ";
                    }
                } else if (i == 6) {
                    if (rule[i] == 1) {
                        line += "{TBR <= 0 = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{TBR <= 0 = FALSE} ";
                    }
                } else if (i == 8) {
                    if (rule[i] == 1) {
                        line += "{VOL <= 0.03 = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{VOL <= 0.03 = FALSE} ";
                    }
                } else if (i == 10) {
                    if (rule[i] == 1) {
                        line += "{MOM > 0 = TRUE} ";
                    } else if (rule[i] == 0) {
                        line += "{MOM > 0 = FALSE} ";
                    }
                }
            }
        }
        if (rule[12] == 1) {
            line += "THEN YES";
        } else {
            line += "THEN NO";
        }
        return line;
    }
}