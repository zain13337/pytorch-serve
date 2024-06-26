package org.pytorch.serve.metrics;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Metric {

    private static final Pattern PATTERN =
            Pattern.compile(
                    "\\s*([\\w\\s]+)\\.([\\w\\s]+):([0-9\\-,.e]+)\\|#([^|]*)(\\|#type:([^|,]+))?\\|#hostname:([^,]+),([^,]+)(,(.*))?");

    @SerializedName("MetricName")
    private String metricName;

    @SerializedName("Value")
    private String value;

    @SerializedName("Unit")
    private String unit;

    @SerializedName("Type")
    private String type;

    @SerializedName("Dimensions")
    private List<Dimension> dimensions;

    @SerializedName("DimensionNames")
    private List<String> dimensionNames;

    @SerializedName("DimensionValues")
    private List<String> dimensionValues;

    @SerializedName("Timestamp")
    private String timestamp;

    @SerializedName("RequestId")
    private String requestId;

    @SerializedName("HostName")
    private String hostName;

    public Metric() {}

    public Metric(
            String metricName,
            String value,
            String unit,
            String type,
            String hostName,
            Dimension... dimensions) {
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.type = type;
        this.hostName = hostName;
        this.setDimensions(Arrays.asList(dimensions));
        this.timestamp =
                String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public List<String> getDimensionNames() {
        return this.dimensionNames;
    }

    public List<String> getDimensionValues() {
        return this.dimensionValues;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
        this.dimensionNames = new ArrayList<String>();
        this.dimensionValues = new ArrayList<String>();
        for (Dimension dimension : dimensions) {
            this.dimensionNames.add(dimension.getName());
            this.dimensionValues.add(dimension.getValue());
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static Metric parse(String line) {
        // DiskAvailable.Gigabytes:311|#Level:Host,hostname:localhost
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        Metric metric = new Metric();
        metric.setMetricName(matcher.group(1));
        metric.setUnit(matcher.group(2));
        metric.setValue(matcher.group(3));
        String dimensions = matcher.group(4);
        metric.setType(matcher.group(6));
        metric.setHostName(matcher.group(7));
        metric.setTimestamp(matcher.group(8));
        metric.setRequestId(matcher.group(10));

        if (dimensions != null) {
            String[] dimension = dimensions.split(",");
            List<Dimension> list = new ArrayList<>(dimension.length);
            for (String dime : dimension) {
                String[] pair = dime.split(":");
                if (pair.length == 2) {
                    list.add(new Dimension(pair[0], pair[1]));
                }
            }
            metric.setDimensions(list);
        }

        return metric;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(metricName).append('.').append(unit).append(':').append(getValue()).append("|#");
        boolean first = true;
        for (Dimension dimension : getDimensions()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(dimension.getName()).append(':').append(dimension.getValue());
        }
        sb.append("|#hostname:").append(hostName);
        if (requestId != null) {
            sb.append(",requestID:").append(requestId);
        }
        sb.append(",timestamp:").append(timestamp);
        return sb.toString();
    }
}
