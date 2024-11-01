package pw.mihou.amelia.commands.subcommands

import org.javacord.api.interaction.SlashCommandInteractionOption
import pw.mihou.Amaririsu
import pw.mihou.amelia.commands.components.ErrorEmbed
import pw.mihou.amelia.commands.components.Loading
import pw.mihou.amelia.commands.components.PaginatedButtons
import pw.mihou.amelia.commands.components.PlainEmbed
import pw.mihou.amelia.commands.components.UserResultEmbed
import pw.mihou.amelia.commands.hooks.useDeletable
import pw.mihou.amelia.commands.hooks.useLoader
import pw.mihou.amelia.feeds.Feeds
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.models.user.UserResultOrAuthor
import pw.mihou.nexus.coroutines.utils.coroutine
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.reakt.adapters.AsyncR

object RegisterAuthorSubcommand {
    fun run(
        event: NexusCommandEvent,
        subcommand: SlashCommandInteractionOption,
    ) {
        if (subcommand.name != "user") {
            return
        }

        val name = subcommand.getArgumentStringValueByName("name").orElseThrow()
        val channel =
            subcommand
                .getArgumentChannelValueByName("channel")
                .flatMap {
                    it.asServerTextChannel()
                }.orElseThrow()

        event.interaction.AsyncR(false) {
            val pageDelegate = writable(0)
            val page by pageDelegate

            val (isLoading, executeLongTask) = this@AsyncR.useLoader()

            var selectResponse: String? by writable(null)

            val deleteDelegate = this@AsyncR.useDeletable

            var results: List<UserResultOrAuthor>? by writable(null)
            var error: String? by writable(null)

            onInitialRender {
                executeLongTask {
                    try {
                        results =
                            Amaririsu
                                .search(name) { series.enabled = false }
                                .users
                                .toList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error = TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE
                    }
                }
            }

            render {
                val err = error
                if (err != null) {
                    ErrorEmbed(err)
                    return@render
                }

                if (selectResponse != null) {
                    PlainEmbed(selectResponse!!)
                    return@render
                }

                if (isLoading.get()) {
                    Loading()
                    return@render
                }

                val userResults = results
                if (userResults == null) {
                    PlainEmbed(
                        TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE,
                    )
                    return@render
                }

                if (userResults.isEmpty()) {
                    PlainEmbed(TemplateMessages.ERROR_NO_USERS_FOUND)
                    return@render
                }

                val user = userResults[page]
                UserResultEmbed(user, page, userResults.size)
                PaginatedButtons(
                    pageDelegate,
                    deleteDelegate,
                    onSelect = {
                        coroutine {
                            executeLongTask {
                                try {
                                    selectResponse =
                                        Feeds.tryRegisterUser(
                                            user.id,
                                            channel,
                                            event.user,
                                            user,
                                        )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    error = e.message
                                }
                            }
                        }
                    },
                    user = requestUser().join()!!,
                    lastPage = userResults.size - 1,
                )
            }
        }
    }
}
