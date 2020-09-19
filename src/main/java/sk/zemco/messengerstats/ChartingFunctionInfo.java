package sk.zemco.messengerstats;

import org.knowm.xchart.CategoryChart;
import sk.zemco.messengerstats.models.RootEntry;

import java.util.function.Function;

public class ChartingFunctionInfo {

    private final String outputName;
    private final Function<RootEntry, CategoryChart> function;

    public ChartingFunctionInfo(String outputName, Function<RootEntry, CategoryChart> function) {
        this.outputName = outputName;
        this.function = function;
    }

    public String getOutputName() {
        return outputName;
    }

    public Function<RootEntry, CategoryChart> getFunction() {
        return function;
    }

}
