package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
// Начало отсчёта времени
        long startTime = System.currentTimeMillis();

        String inputFile = "src\\main\\resources\\Ing.txt";
        String outputFile = "groups_output.txt";

        // Используем HashSet для хранения уникальных строк
        Set<String> uniqueLinesSet = new HashSet<>();
        // Используем ArrayList для индексированного доступа к строкам
        List<String[]> uniqueLinesList = new ArrayList<>();

        // Шаг 1: Чтение файла и очистка данных
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Разделяем строку по ';' и убираем кавычки
                String[] parts = line.trim().split(";");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].replaceAll("^\"|\"$", ""); // Убираем кавычки в начале и конце
                }
                // Объединяем обратно в строку для проверки уникальности
                String joinedLine = String.join(";", parts);
                // Добавляем только уникальные строки
                if (uniqueLinesSet.add(joinedLine)) {
                    uniqueLinesList.add(parts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int numLines = uniqueLinesList.size();
        System.out.println("Количество уникальных строк: " + numLines);

        // Шаг 2: Создание отображений значений колонок к строкам
        // Ключ: "колонка|значение", Значение: набор индексов строк
        Map<String, Set<Integer>> valueToIndices = new HashMap<>();

        for (int idx = 0; idx < uniqueLinesList.size(); idx++) {
            String[] line = uniqueLinesList.get(idx);
            for (int col = 0; col < line.length; col++) {
                String value = line[col];
                if (!value.isEmpty()) { // Игнорируем пустые значения
                    String key = col + "|" + value;
                    valueToIndices.computeIfAbsent(key, k -> new HashSet<>()).add(idx);
                }
            }
        }

        // Шаг 3: Реализация Union-Find для группирования строк
        UnionFind uf = new UnionFind(numLines);

        // Объединяем строки, которые имеют совпадающие значения в одной колонке
        for (Set<Integer> indices : valueToIndices.values()) {
            if (indices.size() > 1) { // Объединяем только если есть более одного индекса
                Iterator<Integer> it = indices.iterator();
                int first = it.next();
                while (it.hasNext()) {
                    int next = it.next();
                    uf.union(first, next);
                }
            }
        }

        // Шаг 4: Сбор групп
        Map<Integer, List<String>> groups = new HashMap<>();
        for (int idx = 0; idx < uniqueLinesList.size(); idx++) {
            int root = uf.find(idx);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(String.join(";", uniqueLinesList.get(idx)));
        }

        // Шаг 5: Фильтрация групп с более чем одним элементом
        List<List<String>> filteredGroups = new ArrayList<>();
        for (List<String> group : groups.values()) {
            if (group.size() > 1) {
                filteredGroups.add(group);
            }
        }

        // Шаг 6: Сортировка групп по убыванию количества элементов
        filteredGroups.sort((g1, g2) -> Integer.compare(g2.size(), g1.size())); // Сортировка по убыванию

        // Шаг 7: Запись групп в файл
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            bw.write("Количество групп с более чем одним элементом: " + filteredGroups.size());
            bw.write("\n\n");
            int groupNumber = 1;
            for (List<String> group : filteredGroups) {
                bw.write("Группа " + groupNumber + "\n");
                for (String groupLine : group) {
                    bw.write(groupLine + "\n");
                }
                bw.write("\n");
                groupNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Конец отсчёта времени
        long endTime = System.currentTimeMillis();
        double executionTimeSeconds = (endTime - startTime) / 1000.0;

        // Вывод результатов
        System.out.println("Количество групп с более чем одним элементом: " + filteredGroups.size());
        System.out.println(String.format("Время выполнения программы: %.2f секунд", executionTimeSeconds));
    }

    // Класс для реализации Union-Find (Система непересекающихся множеств)
    static class UnionFind {
        private int[] parent;

        // Конструктор: инициализируем каждый элемент как корень самого себя
        public UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        // Метод для нахождения корня множества с применением сжатия путей
        public int find(int u) {
            if (parent[u] != u) {
                parent[u] = find(parent[u]); // Рекурсивное сжатие пути
            }
            return parent[u];
        }

        // Метод для объединения двух множеств
        public void union(int u, int v) {
            int rootU = find(u);
            int rootV = find(v);
            if (rootU != rootV) {
                parent[rootV] = rootU; // Объединяем деревья
            }
        }
    }
}