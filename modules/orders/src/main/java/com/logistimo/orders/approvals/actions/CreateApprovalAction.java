/*
 * Copyright © 2017 Logistimo.
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

package com.logistimo.orders.approvals.actions;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.Approver;
import com.logistimo.approvals.client.models.CreateApprovalRequest;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.approvals.utils.ApprovalUtils;
import com.logistimo.orders.approvals.validations.ApprovalRequesterValidator;
import com.logistimo.orders.approvals.validations.OrderApprovalStatusValidator;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class CreateApprovalAction {

  @Autowired
  private ApprovalsBuilder builder;

  @Autowired
  private IApprovalsClient approvalsClient;

  @Autowired
  private IApprovalsDao approvalDao;

  @Autowired
  private OrderManagementService oms;

  public CreateApprovalResponse invoke(ApprovalRequestModel approvalRequestModel)
      throws ServiceException, ObjectNotFoundException, ValidationException {

    Locale locale = SecurityUtils.getLocale();

    IOrder order = oms.getOrder(approvalRequestModel.getOrderId());

    validateApprovalRequest(approvalRequestModel, order, locale);

    List<Approver>
        approvers =
        ApprovalUtils.getApproversForOrderType(order, approvalRequestModel.getApprovalType());

    CreateApprovalRequest approvalRequest = builder.buildApprovalRequest(order,
        approvalRequestModel.getMessage(), approvalRequestModel.getRequesterId(), approvers);

    return createApproval(approvalRequest, approvalRequestModel.getApprovalType());

  }

  private void validateApprovalRequest(ApprovalRequestModel approvalRequest, IOrder order,
                                       Locale locale) throws ValidationException {
    new ApprovalRequesterValidator(approvalRequest, order,
        SecurityUtils.getUserDetails().getUsername(), locale).validate();
    new OrderApprovalStatusValidator(approvalRequest, order, locale).validate();
  }

  private CreateApprovalResponse createApproval(CreateApprovalRequest approvalRequest,
                                                ApprovalType approvalType)
      throws ServiceException, ObjectNotFoundException {
    CreateApprovalResponse approvalResponse = approvalsClient.createApproval(approvalRequest);
      approvalDao.updateOrderApprovalMapping(approvalResponse, approvalType.getValue());
    return approvalResponse;
  }

}
