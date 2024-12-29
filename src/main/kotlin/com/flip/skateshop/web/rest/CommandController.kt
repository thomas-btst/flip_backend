package com.flip.skateshop.web.rest

import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.interfaces.service.CommandServiceInterface
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandPageDto
import com.flip.skateshop.web.rest.dto.CommandStatusDto
import com.flip.skateshop.web.rest.dto.ShortCommandDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands")
class CommandController(
    private val commandService: CommandServiceInterface,
) {
    @PostMapping("/sessions")
    @Operation(summary = "Create a command payment session")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Command created"),
        ApiResponse(responseCode = "409", description = "Cart is empty or address is not set"),
    )
    suspend fun iniCommand(): String = commandService.initCommandForCurrentUser()

    @PostMapping("/sessions/{sessionId}")
    @Operation(summary = "Finalize command for a session")
    @ApiResponses(ApiResponse(responseCode = "200", description = "Session has been finalized"))
    suspend fun finalizeCommand(
        @PathVariable sessionId: String,
    ) = commandService.finalizeCommandForCurrentUser(sessionId)

    @GetMapping
    @Operation(summary = "List commands current for user")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun listCommandsForCurrentUser(): List<ShortCommandDto> = commandService.listCommandsForCurrentUser()

    @GetMapping("/users/{userId}")
    @Operation(summary = "List commands for user")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "User not found"),
    )
    suspend fun listCommands(
        @PathVariable userId: ObjectId,
    ): List<ShortCommandDto> = commandService.listCommandsForCurrentUser()

    @GetMapping("/{commandId}")
    @Operation(summary = "Get a command by its id for current user")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "Command not found"),
    )
    suspend fun getCommand(
        @PathVariable commandId: ObjectId,
    ): CommandDto = commandService.getCommandForCurrentUser(commandId)

    @PatchMapping("/{commandId}/cancel")
    @Operation(summary = "Cancel a pending command")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "404", description = "Command not found or not pending"),
    )
    suspend fun cancelCommand(
        @PathVariable commandId: ObjectId,
    ) = commandService.cancelCommandForCurrentUser(commandId)

    @GetMapping("/limit/{limit}/page/{page}")
    @Operation(summary = "Retrieve a pagination of commands by id and status")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCommandsByPage(
        @PathVariable limit: Int,
        @PathVariable page: Long,
        @RequestParam search: String = "",
        @RequestParam status: CommandStatus?,
    ): CommandPageDto = commandService.getCommandsByPage(limit, page, search, status)

    @GetMapping("/admin/{commandId}")
    @Operation(summary = "Get a command by Id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "Command not found"),
    )
    suspend fun getCommandById(
        @PathVariable commandId: ObjectId,
    ): CommandDto = commandService.getCommand(commandId)

    @PatchMapping("/{commandId}/status")
    @Operation(summary = "Change a command status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "404", description = "Command not found"),
    )
    suspend fun changeCommandStatus(
        @PathVariable commandId: ObjectId,
        @RequestBody commandStatus: CommandStatusDto,
    ) = commandService.updateCommandStatus(commandId, commandStatus.status)
}
