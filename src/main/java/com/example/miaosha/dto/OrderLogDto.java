package com.example.miaosha.dto;

import java.util.Date;

public class OrderLogDto {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.order_log_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private String orderLogId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.item_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.amount
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private Integer amount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.status
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private Integer status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.create_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column order_log.update_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    private Date updateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.order_log_id
     *
     * @return the value of order_log.order_log_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public String getOrderLogId() {
        return orderLogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.order_log_id
     *
     * @param orderLogId the value for order_log.order_log_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setOrderLogId(String orderLogId) {
        this.orderLogId = orderLogId == null ? null : orderLogId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.item_id
     *
     * @return the value of order_log.item_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.item_id
     *
     * @param itemId the value for order_log.item_id
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.amount
     *
     * @return the value of order_log.amount
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.amount
     *
     * @param amount the value for order_log.amount
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.status
     *
     * @return the value of order_log.status
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.status
     *
     * @param status the value for order_log.status
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.create_time
     *
     * @return the value of order_log.create_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.create_time
     *
     * @param createTime the value for order_log.create_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column order_log.update_time
     *
     * @return the value of order_log.update_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column order_log.update_time
     *
     * @param updateTime the value for order_log.update_time
     *
     * @mbg.generated Sun Jul 03 21:51:52 CST 2022
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}