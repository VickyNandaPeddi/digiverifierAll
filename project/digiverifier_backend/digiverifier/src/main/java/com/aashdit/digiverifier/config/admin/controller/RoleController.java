package com.aashdit.digiverifier.config.admin.controller;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.dto.RoleHeadPermissionListDto;
import com.aashdit.digiverifier.config.admin.dto.RolePermissionDTO;
import com.aashdit.digiverifier.config.admin.model.Role;
import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.service.RoleService;
import com.aashdit.digiverifier.utils.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/api/role")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

   @Operation(summary = "Get All Role")
    @GetMapping("/getAllRole")
    public ResponseEntity<ServiceOutcome<List<Role>>> getAllRole(@RequestHeader("Authorization") String authorization) {
        ServiceOutcome<List<Role>> svcSearchResult = roleService.getAllRole();
        return new ResponseEntity<ServiceOutcome<List<Role>>>(svcSearchResult, HttpStatus.OK);
    }

   @Operation(summary = "Get Role Access List for user Login")
    @GetMapping("/getRoleDropDownByUser")
    public ResponseEntity<ServiceOutcome<List<Role>>> getRoleDropDownByUser(@RequestHeader("Authorization") String authorization) {
        ServiceOutcome<List<Role>> svcSearchResult = roleService.getRoleDropDownByUser();
        return new ResponseEntity<ServiceOutcome<List<Role>>>(svcSearchResult, HttpStatus.OK);
    }

//	@ApiOperation("Save and Update Role Master")
//	@PostMapping(path = "/saveNUpdateRole")
//	public ResponseEntity<ServiceOutcome<Role>> saveNUpdateRole(@RequestBody Role role,@RequestHeader("Authorization") String authorization) {
//		ServiceOutcome<Role> svcSearchResult = roleService.saveNUpdateRole(role);
//		return new ResponseEntity<ServiceOutcome<Role>>(svcSearchResult, HttpStatus.OK);
//	}

   @Operation(summary = "Get All Details for Role Permission")
    @GetMapping("/getAllRolePermission")
    public ResponseEntity<ServiceOutcome<List<RoleHeadPermissionListDto>>> getAllRolePermission(@RequestHeader("Authorization") String authorization) {
        ServiceOutcome<List<RoleHeadPermissionListDto>> svcSearchResult = roleService.getAllRolePermission();
        ;
        return new ResponseEntity<ServiceOutcome<List<RoleHeadPermissionListDto>>>(svcSearchResult, HttpStatus.OK);
    }

   @Operation(summary = "Save Role Permission Mapping")
    @PostMapping(path = "/rolePermission")
    public ResponseEntity<ServiceOutcome<RolePermissionDTO>> rolePermission(@RequestBody RolePermissionDTO rolePermissionDTO, @RequestHeader("Authorization") String authorization) {
        ServiceOutcome<RolePermissionDTO> svcSearchResult = roleService.rolePermission(rolePermissionDTO);
        return new ResponseEntity<ServiceOutcome<RolePermissionDTO>>(svcSearchResult, HttpStatus.OK);
    }

   @Operation(summary = "Get All Details Role Permission Mapping after Save")
    @GetMapping("/getAllUserRolePerMissionMap/{roleId}")
    public ResponseEntity<ServiceOutcome<RolePermissionDTO>> getAllUserRolePerMissionMap(@PathVariable("roleId") Long roleId, @RequestHeader("Authorization") String authorization) {
        ServiceOutcome<RolePermissionDTO> svcSearchResult = roleService.getAllUserRolePerMissionMap(roleId);
        return new ResponseEntity<ServiceOutcome<RolePermissionDTO>>(svcSearchResult, HttpStatus.OK);
    }

   @Operation(summary = "Returns role code of current user after login")
    @PostMapping(path = "/getRoleCode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceOutcome<String>> getRoleCode(@RequestHeader("Authorization") String authorization) throws Exception {

        ServiceOutcome<String> response = new ServiceOutcome<>();
        try {
            String roleCode = "";
            User user = SecurityHelper.getCurrentUser();
            if (user != null) {
                roleCode = user.getRole().getRoleCode();
                response.setData(roleCode.split("_")[1]);
            }
            response.setOutcome(true);
            response.setMessage("Role code found and sent successfully.");

        } catch (Exception ex) {
            response.setData(null);
            response.setOutcome(false);
            response.setMessage("Role code not found.");
            log.error("Exception occured in dashboardRole method in RoleController-->" + ex);
        }

        return new ResponseEntity<ServiceOutcome<String>>(response, HttpStatus.OK);
    }

   @Operation(summary = "Get All Role Permission codes ")
    @GetMapping("/getRolePerMissionCodes/{roleCode}")
    public ResponseEntity<ServiceOutcome<List<String>>> getAllRolePerMissionCodeByRoleCode(@PathVariable("roleCode") String roleCode, @RequestHeader("Authorization") String authorization) {
        ServiceOutcome<List<String>> svcSearchResult = roleService.getAllRolePerMissionCodeByRoleCode(roleCode);
        return new ResponseEntity<ServiceOutcome<List<String>>>(svcSearchResult, HttpStatus.OK);
    }

}
