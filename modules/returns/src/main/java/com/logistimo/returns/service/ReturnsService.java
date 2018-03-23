/*
 * Copyright © 2018 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.returns.service;

import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.ReturnsConfig;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.materials.model.HandlingUnitModel;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.returns.Status;
import com.logistimo.returns.transactions.ReturnsTransactionHandler;
import com.logistimo.returns.models.ReturnsFilters;
import com.logistimo.returns.models.UpdateStatusModel;
import com.logistimo.returns.validators.ReturnsValidator;
import com.logistimo.returns.vo.ReturnsItemBatchVO;
import com.logistimo.returns.vo.ReturnsItemVO;
import com.logistimo.returns.vo.ReturnsStatusVO;
import com.logistimo.returns.vo.ReturnsVO;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.shipments.FulfilledQuantityModel;
import com.logistimo.shipments.service.impl.ShipmentService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jdo.PersistenceManager;

/**
 * Created by pratheeka on 13/03/18.
 */
@Service
@EnableTransactionManagement

public class ReturnsService {


  @Autowired
  private ReturnsValidator returnsValidator;

  @Autowired
  private DemandService demandService;

  @Autowired
  private ReturnsRepository returnsDao;

  @Autowired
  private ActivityService activityService;

  @Autowired
  private ConversationService conversationService;

  @Autowired
  private InventoryManagementService inventoryManagementService;


  @Autowired
  ShipmentService shipmentService;

  @Autowired
  IHandlingUnitService handlingUnitService;

  @Autowired
  OrderManagementService orderManagementService;


  @Transactional(transactionManager = "transactionManager")
  public ReturnsVO createReturns(ReturnsVO returnsVO) throws ServiceException {

    List<ReturnsItemVO> returnsItemVOList = returnsVO.getItems();

    validateQuantity(returnsVO, returnsItemVOList);

    validateReturnsPolicy(returnsVO.getOrderId(),returnsVO.getVendorId());

    Map<Long, BigDecimal> returnedQuantity = new HashMap<>();
    returnsDao.saveReturns(returnsVO);
    Long returnId = returnsVO.getId();
    returnsItemVOList.forEach(returnsItemVO -> {
      returnsItemVO.setReturnsId(returnId);
      returnsDao.saveReturnsItems(returnsItemVO);
      BigDecimal quantity = BigDecimal.ZERO;
      List<ReturnsItemBatchVO>
          returnsItemBatchVOList = returnsItemVO.getReturnItemBatches();
      if (CollectionUtils.isNotEmpty(returnsItemBatchVOList)) {
        Long itemId = returnsItemVO.getId();
        quantity = returnsItemBatchVOList.stream().map(returnsItemBatchVO -> {
          returnsItemBatchVO.setItemId(itemId);
          returnsDao.saveReturnBatchItems(returnsItemBatchVO);
          return returnsItemBatchVO.getQuantity();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        returnsItemVO.setReturnItemBatches(returnsItemBatchVOList);
      } else {
        quantity = returnsItemVO.getQuantity();
      }
      returnedQuantity.put(returnsItemVO.getMaterialId(), quantity);
    });

    demandService.updateDemandReturns(returnsVO.getOrderId(), returnedQuantity,false);

    IMessage
        message =
        addComment(returnsVO.getId(), returnsVO.getComment(), returnsVO.getCreatedBy(),
            returnsVO.getSourceDomain());
    addStatusHistory(returnsVO, null, returnsVO.getStatus().getStatus().toString(), message);

    return returnsVO;
  }

  private void validateQuantity(ReturnsVO returnsVO, List<ReturnsItemVO> returnsItemVOList)
      throws ServiceException {
    if (CollectionUtils.isEmpty(returnsItemVOList)) {
      throw new ValidationException("RT001", "Items list cannot be empty!!");
      //TODO
    }
    List<Long> materialIdList =
        returnsItemVOList.stream().map(ReturnsItemVO::getMaterialId)
            .collect(Collectors.toList());

    List<FulfilledQuantityModel> shipmentList =
        shipmentService.getFulfilledQuantityByOrderId(returnsVO.getOrderId(), materialIdList);

    List<HandlingUnitModel>
        handlingUnitModelList =
        handlingUnitService.getHandlingUnitDataByMaterialIds(materialIdList);

    returnsValidator
        .validateReturnedQuantity(returnsVO.getItems(), shipmentList, handlingUnitModelList);
  }

  private void validateReturnsPolicy(Long orderId, Long vendorId) throws ServiceException {

    IOrder order = orderManagementService.getOrder(orderId);
    Optional<ReturnsConfig>
        returnsConfiguration =
        inventoryManagementService.getReturnsConfig(vendorId);
    if (!returnsConfiguration.isPresent()) {
      return;
    }

    returnsValidator
        .validateReturnsPolicy(returnsConfiguration.get(), order.getStatusUpdatedOn().getTime());

  }

  public List<ReturnsItemVO> getReturnsItem(Long returnId) {
    if (returnId == null) {
      throw new InvalidDataException("Returns Id cannot be null");
    }
    return returnsDao.getReturnedItems(returnId);
  }

  @Transactional
  public ReturnsVO updateReturnsStatus(UpdateStatusModel statusModel)
      throws ServiceException, DuplicationException {
    ReturnsVO returnsVO = returnsDao.getReturnsById(statusModel.getReturnId());
    Status newStatus = statusModel.getStatus();
    Status oldStatus = returnsVO.getStatus().getStatus();
    returnsValidator.validateStatusChange(newStatus, oldStatus);
    if (returnsValidator.checkAccessForStatusChange(statusModel, returnsVO)) {
      buildReturns(statusModel, returnsVO, newStatus);
      returnsDao.updateReturns(returnsVO);
      returnsVO.setItems(getReturnsItem(returnsVO.getId()));
      updateDemandItems(returnsVO,statusModel.getStatus());
      postTransactions(statusModel, returnsVO);
      IMessage message = null;
      if (StringUtils.isNotBlank(statusModel.getComment())) {
        message =
            addComment(returnsVO.getId(), statusModel.getComment(), returnsVO.getUpdatedBy(),
                returnsVO.getSourceDomain());
      }
      addStatusHistory(returnsVO, oldStatus.toString(), newStatus.toString(), message);
    }
    return returnsVO;
  }

  private void updateDemandItems(ReturnsVO returnsVO,Status status) {
    if(status==Status.CANCELLED) {
      Map<Long, BigDecimal> returnedQuantity = new HashMap<>();
      returnsVO.getItems().forEach(returnsItemVO -> {
        BigDecimal quantity = BigDecimal.ZERO;
        if (returnsItemVO.getReturnItemBatches() != null) {
          quantity =
              returnsItemVO.getReturnItemBatches().stream().map(ReturnsItemBatchVO::getQuantity)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
          quantity = returnsItemVO.getQuantity();
        }
        returnedQuantity.put(returnsItemVO.getMaterialId(), quantity);
      });
      demandService.updateDemandReturns(returnsVO.getOrderId(), returnedQuantity, true);
    }
  }


  private void buildReturns(UpdateStatusModel statusModel, ReturnsVO returnsVO,
                            Status newStatus) {
    ReturnsStatusVO statusVO = new ReturnsStatusVO();
    statusVO.setStatus(newStatus);
    Timestamp updatedAt = new Timestamp(new Date().getTime());
    statusVO.setUpdatedAt(updatedAt);
    statusVO.setUpdatedBy(statusModel.getUserId());
    returnsVO.setStatus(statusVO);
    returnsVO.setUpdatedBy(statusModel.getUserId());
    returnsVO.setUpdatedAt(updatedAt);
  }

  private void postTransactions(UpdateStatusModel statusModel, ReturnsVO returnsVO)
      throws ServiceException, DuplicationException {
    if (statusModel.getStatus() == Status.RECEIVED || statusModel.getStatus() == Status.SHIPPED) {
      Long domainId = SecurityUtils.getCurrentDomainId();
          new ReturnsTransactionHandler().postTransactions(statusModel, returnsVO, domainId);
    }
  }

  public ReturnsVO getReturnsById(Long returnId) {
    ReturnsVO returnsVO = returnsDao.getReturnsById(returnId);
    returnsVO.setItems(getReturnsItem(returnId));
    return returnsVO;
  }

  public List<ReturnsVO> getReturns(ReturnsFilters filters) {
    return returnsDao.getReturns(filters);
  }

  private void addStatusHistory(ReturnsVO returnVO, String oldStatus, String newStatus,
                                IMessage iMessage) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      activityService
          .createActivity(IActivity.TYPE.RETURNS.name(), String.valueOf(returnVO.getId()),
              "STATUS", oldStatus, newStatus, returnVO.getCreatedBy(), returnVO.getSourceDomain(),
              iMessage != null ? iMessage.getMessageId() : null, "RETURNS:" + returnVO.getId(), pm);
    } finally {
      pm.close();
    }
  }

  public IMessage addComment(Long returnId, String message, String userId, Long domainId)
      throws ServiceException {
    if (message != null) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        return conversationService.addMsgToConversation(IActivity.TYPE.RETURNS.name(),
            String.valueOf(returnId), message, userId, Collections.singleton("RETURNS:" + returnId)
            , domainId, pm);
      } finally {
        pm.close();
      }
    }
    return null;
  }

  public Long getReturnsCount(ReturnsFilters returnsFilters) {
    return returnsDao.getReturnsCount(returnsFilters);
  }

}
