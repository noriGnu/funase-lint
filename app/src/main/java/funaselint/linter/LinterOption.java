package funaselint.linter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import funaselint.rules.Rule;

public class LinterOption {
    private boolean fix = false;
    private boolean verbose = false;
    private List<Rule> rules;
    private String style;

    public LinterOption() {
    }

    public void loadFromConfigFile(File configFile) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFile)) {
            LinterOption options = gson.fromJson(reader, LinterOption.class);
            this.fix = options.fix;
            this.verbose = options.verbose;
            this.rules = options.rules;
            this.style = options.style;
        }
    }

    public void setFix(boolean fix) {
        this.fix = fix;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isFix() {
        return fix;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public String getStyle() {
        return style;
    }

}
