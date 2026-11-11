package com.orchard.controller;

import com.orchard.annotation.RoleRequired;
import com.orchard.common.Result;
import com.orchard.service.PointsService;
import org.springframework.web.bind.annotation.*;

/** 会员积分（二期） */
@RestController
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @RoleRequired({"USER", "MERCHANT", "ADMIN"})
    @GetMapping("/api/points/mine")
    public Result<?> mine(@RequestAttribute("uid") Long uid,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(pointsService.overview(uid, page, size));
    }
}
