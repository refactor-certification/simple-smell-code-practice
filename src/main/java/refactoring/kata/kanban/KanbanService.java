package refactoring.kata.kanban;

import org.apache.commons.lang3.StringUtils;
import refactoring.kata.kanban.dto.Kanban;
import refactoring.kata.kanban.dto.KanbanColumn;
import refactoring.kata.kanban.dto.KanbanUser;
import refactoring.kata.kanban.dto.QueryBuilder;
import refactoring.kata.kanban.dto.TaskInfo;
import refactoring.kata.kanban.service.ColumnService;
import refactoring.kata.kanban.service.QueryService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  <a href="mailto:meixuesong@gmail.com">Mei Xuesong</a>
 */
public class KanbanService {
    private ColumnService columnService;
    private QueryService queryService;

    public KanbanService(ColumnService columnService, QueryService queryService) {
        this.columnService = columnService;
        this.queryService = queryService;
    }

    /**
     * 查询某个团队的任务看板。
     * @param search 搜索关键字
     * @param teamId 团队ID
     * @return 该团队的看板
     */
    //TODO: 过长方法
    public Kanban query(String search,
                        String teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("Team can not be null");
        }
        Kanban kanban = new Kanban();
        //查询该团队有哪些任务状态，每个状态对应看板的一列。
        List<TaskInfo> states = columnService.getTaskStatesByTeamId(teamId);
        List<KanbanColumn> columns = new ArrayList<>();
        for (TaskInfo state : states) {
            KanbanColumn column = new KanbanColumn();
            column.setStateId(state.getId());
            column.setName(state.getName());
            column.setSequence(state.getSequence());
            column.setTasks(new ArrayList<>());
            columns.add(column);
        }
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtils.isNotEmpty(search)) {
            queryBuilder.should().match("summary", search);
            queryBuilder.should().match("description", search);
        }
        queryBuilder.filter().term("teamId", teamId).term("achieved", "FALSE");
        //查询符合条件的任务
        List<TaskInfo> taskList = queryService.queryDocs("TASKS", queryBuilder, 1, 10000, TaskInfo.class);
        List<KanbanUser> users = new ArrayList<>();
        for (TaskInfo task : taskList) {
            if (task.getAssignToUser() != null) {
                KanbanUser kanbanUser = new KanbanUser();
                kanbanUser.setCode(task.getAssignToUser().getLoginCode());
                kanbanUser.setUserName(task.getAssignToUser().getName());
                kanbanUser.setAvatar(task.getAssignToUser().getAvatar());
                users.add(kanbanUser);
            }
            //将任务加到看板对应的列中。
            for (KanbanColumn column : columns) {
                if (column.getStateId().equals(task.getState().getId())) {
                    column.getTasks().add(task);
                }
            }
        }
        kanban.setUsers(users);
        kanban.setColumns(columns);

        return kanban;
    }
}
