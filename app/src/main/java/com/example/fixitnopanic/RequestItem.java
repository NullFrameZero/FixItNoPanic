package com.example.fixitnopanic;

import java.util.Objects;

public class RequestItem {
    private final long id;
    private final String client;
    private final String phone;
    private final String model;
    private final String problem;
    private final String dateCreated;
    private final String dateCompleted;
    private final String status;

    public RequestItem(long id, String client, String phone, String model, String problem,
                       String dateCreated, String dateCompleted, String status) {
        this.id = id;
        this.client = client;
        this.phone = phone;
        this.model = model;
        this.problem = problem;
        this.dateCreated = dateCreated;
        this.dateCompleted = dateCompleted;
        this.status = status;
    }

    // Геттеры
    public long getId() { return id; }
    public String getClient() { return client; }
    public String getPhone() { return phone; }
    public String getModel() { return model; }
    public String getProblem() { return problem; }
    public String getDateCreated() { return dateCreated; }
    public String getDateCompleted() { return dateCompleted; }
    public String getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestItem)) return false;
        RequestItem that = (RequestItem) o;
        return id == that.id &&
                Objects.equals(client, that.client) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(model, that.model) &&
                Objects.equals(problem, that.problem) &&
                Objects.equals(dateCreated, that.dateCreated) &&
                Objects.equals(dateCompleted, that.dateCompleted) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, client, phone, model, problem, dateCreated, dateCompleted, status);
    }

    @Override
    public String toString() {
        return "RequestItem{" +
                "id=" + id +
                ", client='" + client + '\'' +
                ", phone='" + phone + '\'' +
                ", model='" + model + '\'' +
                ", problem='" + problem + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", dateCompleted='" + dateCompleted + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}